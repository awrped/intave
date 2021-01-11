package de.jpx3.intave.detect.checks.world;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.google.common.collect.Maps;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.detect.IntaveMetaCheck;
import de.jpx3.intave.event.packet.ListenerPriority;
import de.jpx3.intave.event.packet.PacketDescriptor;
import de.jpx3.intave.event.packet.PacketSubscription;
import de.jpx3.intave.event.packet.Sender;
import de.jpx3.intave.tools.sync.Synchronizer;
import de.jpx3.intave.tools.wrapper.WrappedBlockPosition;
import de.jpx3.intave.tools.wrapper.WrappedEnumDirection;
import de.jpx3.intave.tools.wrapper.WrappedMovingObjectPosition;
import de.jpx3.intave.tools.wrapper.WrappedVector;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserCustomCheckMeta;
import de.jpx3.intave.user.UserMetaMovementData;
import de.jpx3.intave.world.collision.BoundingBoxAccess;
import de.jpx3.intave.world.raytrace.Raytracer;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType.START_DESTROY_BLOCK;
import static com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType.STOP_DESTROY_BLOCK;

public final class InteractionRaytrace extends IntaveMetaCheck<InteractionRaytrace.InteractionMeta> {
  private final IntavePlugin plugin;

  public InteractionRaytrace(IntavePlugin plugin) {
    super("InteractionRaytrace", "interactionRaytrace", InteractionMeta.class);
    this.plugin = plugin;
  }

  // break
  // 1st invalid -> save packet and queue packet for 2nd move (--> cancel)

  // interact
  // 1st invalid -> save raytrace and queue packet for 2nd move packet (--> raytrace override)

  // place
  // 1st invalid -> save raytrace and queue packet for 2nd move packet (--> raytrace override)

  @PacketSubscription(
    priority = ListenerPriority.LOW,
    packets = {
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "BLOCK_PLACE"),
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "USE_ITEM")
    }
  )
  public void receiveInteraction(PacketEvent event) {
    Player player = event.getPlayer();
    PacketContainer packet = event.getPacket();
    BlockPosition blockPosition = packet.getBlockPositionModifier().readSafely(0);
    if(blockPosition == null) {
      return;
    }
    Integer enumDirection = packet.getIntegers().readSafely(0);
    if(enumDirection == null) {
      enumDirection = packet.getDirections().readSafely(0).ordinal();
    }
    if(enumDirection == 255) {
      return;
    }

    User user = userOf(player);
    InteractionMeta interactionMeta = metaOf(user);
    UserMetaMovementData movementData = user.meta().movementData();

    Location playerLocation = movementData.verifiedLocation().clone();
    playerLocation.setYaw(movementData.rotationYaw);
    playerLocation.setPitch(movementData.rotationPitch);
    Material itemTypeInHand = player.getItemInHand().getType();
    boolean isPlacement = itemTypeInHand != Material.AIR && itemTypeInHand.isBlock();

    Interaction interaction = new Interaction(
      player.getWorld(), player, blockPosition, enumDirection, packet.deepClone(),
      isPlacement ? InteractionType.PLACE : InteractionType.INTERACT
    );
    interactionMeta.interactionList.add(interaction);
    event.setCancelled(true);

    // when nothing is sent
    Synchronizer.synchronize(() -> {
      Location location = new Location(player.getWorld(), movementData.lastPositionX, movementData.lastPositionY, movementData.lastPositionZ);
      location.setYaw(movementData.rotationYaw);
      location.setPitch(movementData.rotationPitch);

      Location mouseDelayPlayerLocation = location.clone();
      mouseDelayPlayerLocation.setYaw(movementData.lastRotationYaw);

      WrappedMovingObjectPosition raycastResult = Raytracer.blockRayTrace(player, location);
      WrappedMovingObjectPosition raycastResultmdf = Raytracer.blockRayTrace(player, mouseDelayPlayerLocation);
      processTraceReport(interaction, raycastResult, raycastResultmdf, true);
    });
  }

  @PacketSubscription(
    priority = ListenerPriority.LOW,
    packets = {
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "BLOCK_DIG")
    }
  )
  public void receiveBreak(PacketEvent event) {
    Player player = event.getPlayer();
    PacketContainer packet = event.getPacket();
    BlockPosition blockPosition = packet.getBlockPositionModifier().readSafely(0);
    if(blockPosition == null) {
      return;
    }

    EnumWrappers.PlayerDigType playerDigType = packet.getPlayerDigTypes().readSafely(0);
    if(!(playerDigType == START_DESTROY_BLOCK || playerDigType == STOP_DESTROY_BLOCK)) {
      return;
    }

    EnumWrappers.Direction direction = packet.getDirections().readSafely(0);
    int enumDirection = direction == null ? 255 : direction.ordinal();
    if(enumDirection == 255) {
      return;
    }

    User user = userOf(player);
    InteractionMeta interactionMeta = metaOf(user);
    UserMetaMovementData movementData = user.meta().movementData();

    Interaction interaction = new Interaction(
      player.getWorld(), player, blockPosition, enumDirection, packet.deepClone(), InteractionType.BREAK
    );
    interactionMeta.interactionList.add(interaction);
    event.setCancelled(true);

    // when nothing is sent
    Synchronizer.synchronize(() -> {
      Location location = new Location(player.getWorld(), movementData.lastPositionX, movementData.lastPositionY, movementData.lastPositionZ);
      location.setYaw(movementData.rotationYaw);
      location.setPitch(movementData.rotationPitch);

      Location mouseDelayPlayerLocation = location.clone();
      mouseDelayPlayerLocation.setYaw(movementData.lastRotationYaw);

      WrappedMovingObjectPosition raycastResult = Raytracer.blockRayTrace(player, location);
      WrappedMovingObjectPosition raycastResultmdf = Raytracer.blockRayTrace(player, mouseDelayPlayerLocation);
      processTraceReport(interaction, raycastResult, raycastResultmdf, true);
    });
  }

  @PacketSubscription(
    priority = ListenerPriority.MONITOR, // last one to work with position
    packets = {
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "LOOK"),
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "FLYING"),
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "POSITION"),
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "POSITION_LOOK")
    }
  )
  public void receivePositionUpdate(PacketEvent event) {
    Player player = event.getPlayer();
    World world = player.getWorld();
    User user = userOf(player);
    UserMetaMovementData movementData = user.meta().movementData();
    InteractionMeta interactionMeta = metaOf(user);
    List<Interaction> interactionList = interactionMeta.interactionList;

    if(interactionList.isEmpty()) {
      return;
    }

    Location playerLocation = new Location(world, movementData.lastPositionX, movementData.lastPositionY, movementData.lastPositionZ);
    playerLocation.setYaw(movementData.rotationYaw);
    playerLocation.setPitch(movementData.rotationPitch);

    Location playerLocationmdf = playerLocation.clone();
    playerLocationmdf.setYaw(movementData.lastRotationYaw);

    WrappedMovingObjectPosition raycastResult = Raytracer.blockRayTrace(player, playerLocation);
    WrappedMovingObjectPosition raycastResultmdf = Raytracer.blockRayTrace(player, playerLocationmdf);

    for (Interaction interaction : interactionList) {
      processTraceReport(interaction, raycastResult, raycastResultmdf, false);
    }
    interactionList.clear();
  }

  private void processTraceReport(
    Interaction interaction,
    WrappedMovingObjectPosition raycastResult,
    WrappedMovingObjectPosition raycastResultmdf,
    boolean delayed
  ) {
    if(interaction.entered()) {
      return;
    }
    interaction.enter();

    World world = interaction.world();
    Player player = interaction.player();
    InteractionMeta interactionMeta = metaOf(player);

    boolean hitMiss = interactionMeta.estimateMouseDelayFix ? raycastResultmdf == null || raycastResultmdf.hitVec == WrappedVector.ZERO : raycastResult == null || raycastResult.hitVec == WrappedVector.ZERO;
    WrappedBlockPosition raycastVector = hitMiss ? WrappedBlockPosition.ORIGIN : raycastResult.getBlockPos();
    Location raycastLocation = raycastVector.toLocation(world);
    Location targetLocation = interaction.targetBlock.toLocation(world);

    boolean invalid = hitMiss ||
      raycastLocation.distance(targetLocation) > 0 ||
      interaction.targetDirection != raycastResult.sideHit.getIndex();

    // mouse delay fix (on/off)
    if(invalid) {
      boolean hitMiss2 = interactionMeta.estimateMouseDelayFix ? raycastResultmdf == null || raycastResultmdf.hitVec == WrappedVector.ZERO : raycastResultmdf == null || raycastResultmdf.hitVec == WrappedVector.ZERO;
      WrappedBlockPosition raycastVector2 = hitMiss2 ? WrappedBlockPosition.ORIGIN : raycastResultmdf.getBlockPos();
      Location raycastLocation2 = raycastVector2.toLocation(world);

      invalid = hitMiss2 ||
        raycastLocation2.distance(targetLocation) > 0 ||
        interaction.targetDirection != raycastResultmdf.sideHit.getIndex();

      if(invalid) {
        raycastResult = raycastResultmdf;
        hitMiss = hitMiss2;
        raycastLocation = raycastLocation2;
      }

      interactionMeta.estimateMouseDelayFix = !invalid;
    }

    boolean flag = invalid && performFlag(interaction, raycastResult, targetLocation, raycastLocation, hitMiss, delayed);
    emulatePacket(interaction, raycastResult, targetLocation, raycastLocation, hitMiss, flag);
  }

  private void emulatePacket(
    Interaction interaction,
    WrappedMovingObjectPosition raycastResult,
    Location targetLocation,
    Location raycastLocation,
    boolean hitMiss,
    boolean punishment
  ) {
    Player player = interaction.player();
    ResponseType response = interaction.type().response();
    BoundingBoxAccess boundingBoxAccess = userOf(player).boundingBoxAccess();

//    if(punishment) {
//      boundingBoxAccess.identityInvalidate();
//    }


    if(response == ResponseType.RAYTRACE_CAST) {
      if(hitMiss) {
        player.sendMessage("Emulation " + raycastLocation + " " + targetLocation);
        refreshBlocksAround(player, targetLocation);
        boundingBoxAccess.invalidateOverride(interaction.world, targetLocation.getBlockX(), targetLocation.getBlockY(), targetLocation.getBlockZ());
      } else {
        PacketContainer packet = interaction.thePacket();

        if(punishment) {
          if(packet.getDirections().size() > 0) {
            packet.getDirections().write(0, raycastResult.sideHit.toDirection());
          } else {
            packet.getIntegers().write(0, raycastResult.sideHit.getIndex());
          }
          packet.getBlockPositionModifier().write(
            0,
            new BlockPosition(raycastLocation.getBlockX(), raycastLocation.getBlockY(), raycastLocation.getBlockZ())
          );

          boundingBoxAccess.invalidate(targetLocation.getBlockX(), targetLocation.getBlockY(), targetLocation.getBlockZ());
          boundingBoxAccess.invalidateOverride(interaction.world, targetLocation.getBlockX(), targetLocation.getBlockY(), targetLocation.getBlockZ());
        } else {
          boundingBoxAccess.invalidate(targetLocation.getBlockX(), targetLocation.getBlockY(), targetLocation.getBlockZ());
        }

        receiveExcludedPacket(player, packet);
        Synchronizer.synchronize(() -> refreshBlocksAround(player, targetLocation));
      }
    } else {
      if (punishment) {
        refreshBlocksAround(player, targetLocation);
        boundingBoxAccess.invalidateOverride(interaction.world, targetLocation.getBlockX(), targetLocation.getBlockY(), targetLocation.getBlockZ());
      } else {
        Synchronizer.synchronize(() -> receiveExcludedPacket(player, interaction.thePacket));
      }
    }
  }

  private void refreshBlocksAround(Player player, Location targetLocation) {
    player.updateInventory();
    refreshBlock(player, targetLocation);
    for (WrappedEnumDirection direction : WrappedEnumDirection.values()) {
      Location placedBlock = targetLocation.clone().add(direction.getDirectionVec().convertToBukkitVec());
      refreshBlock(player, placedBlock);
    }
  }

  private boolean performFlag(
    Interaction interaction,
    WrappedMovingObjectPosition raycastResult,
    Location targetLocation,
    Location raycastLocation,
    boolean hitMiss,
    boolean delayed
  ) {
    Player player = interaction.player();
    InteractionType type = interaction.type();

    String flagMessage;
    if(type == InteractionType.BREAK) {
      String typeName = targetLocation.getBlock().getType().name().toLowerCase().replace("_", "").replace("block", "");
      String append = "";
      if (hitMiss || (raycastLocation.getBlockX() == 0 && raycastLocation.getBlockY() == 0 && raycastLocation.getBlockZ() == 0)) {
        append = " (looking in air)";
      } else if(raycastLocation.distance(targetLocation) > 0 && raycastLocation.getBlock().getType() != Material.AIR) {
        String blockName = raycastLocation.getBlock().getType().name().toLowerCase().replace("_", "").replace("block", "");
        if(raycastLocation.getBlock().getType() == targetLocation.getBlock().getType()) {
          blockName = "a different " + blockName;
        }
        append = " (looking at " + blockName + " block)";
      } else if (interaction.targetDirection != raycastResult.sideHit.getIndex()){
        append = " (invalid block face)";
      }
      flagMessage = "broke a " + typeName + " block out of sight" + append;
    } else if(type == InteractionType.PLACE) {
      String typeAgainstName = shortenTypeName(targetLocation.getBlock().getType());
      String typeName = shortenTypeName(player.getItemInHand().getType());
      flagMessage = "tried to placed a " + typeName + " block against a " + typeAgainstName + " block out of sight";
    } else {
      String typeAgainstName = shortenTypeName(targetLocation.getBlock().getType());
      flagMessage = "tried to interact with a " + typeAgainstName + " block out of sight";
    }

    flagMessage += " (mdf: "+metaOf(player).estimateMouseDelayFix+")";

    if(delayed) {
      flagMessage += " (timeout)";
    }

    return plugin.retributionService().markPlayer(player, 0, name(), flagMessage);
  }

  private String shortenTypeName(Material type) {
    return type.name().toLowerCase().replace("_", "").replace("block", "");
  }

  private void refreshBlock(Player player, Location location) {
    PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.BLOCK_CHANGE);
    Block block = location.getBlock();
    WrappedBlockData blockData = WrappedBlockData.createData(block.getType(), block.getData());
    BlockPosition position = new BlockPosition(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    packet.getBlockData().write(0, blockData);
    packet.getBlockPositionModifier().write(0, position);
    try {
      ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
    } catch (InvocationTargetException exception) {
      exception.printStackTrace();
    }
  }

  private void receiveExcludedPacket(Player player, PacketContainer packet) {
    try {
      userOf(player).ignoreNextPacket();
      ProtocolLibrary.getProtocolManager().recieveClientPacket(player, packet);
    } catch (InvocationTargetException | IllegalAccessException exception) {
      exception.printStackTrace();
    }
  }

/*  public boolean processViolation(Player player, InteractionType type) {
    boolean cancel = false;
    InteractionMeta interactionMeta = metaOf(player);
    if(type.bufferAvailable) {
      int vl = interactionMeta.violationLevel.computeIfAbsent(type, x -> 0);
      vl = MathHelper.minmax(0, vl + 1,8);
//      player.sendMessage(type + ": " + vl);
      if(vl > 3) {
        cancel = true;
      }
      interactionMeta.violationLevel.put(type, vl);
    } else {
      cancel = true;
    }
    return cancel;
  }

  public void lowerViolation(Player player, InteractionType type) {
    if(!type.bufferAvailable) {
      return;
    }
    InteractionMeta interactionMeta = metaOf(player);
    int vl = interactionMeta.violationLevel.computeIfAbsent(type, x -> 0);
    vl = MathHelper.minmax(0, vl - 1,8);
    interactionMeta.violationLevel.put(type, vl);
  }*/

  public static class InteractionMeta extends UserCustomCheckMeta {
    final List<Interaction> interactionList = new ArrayList<>();
    final Map<InteractionType, Integer> violationLevel = Maps.newEnumMap(InteractionType.class);

    public long lastPlacement;
    public boolean estimateMouseDelayFix = false;
  }

  public enum InteractionType {
    PLACE(ResponseType.RAYTRACE_CAST, false),
    BREAK(ResponseType.CANCEL, false),
    INTERACT(ResponseType.RAYTRACE_CAST, false);

    final ResponseType response;
    final boolean bufferAvailable;

    InteractionType(ResponseType response, boolean bufferAvailable) {
      this.response = response;
      this.bufferAvailable = bufferAvailable;
    }

    public ResponseType response() {
      return response;
    }
  }

  public enum ResponseType {
    RAYTRACE_CAST,
    CANCEL
  }

  public static class Interaction {
    private final World world;
    private final Player player;
    private final BlockPosition targetBlock;
    private final int targetDirection;
    private final PacketContainer thePacket;
    private final InteractionType type;
    private boolean entered = false;

    public Interaction(
      World world, Player player,
      BlockPosition targetBlock,
      int targetDirection,
      PacketContainer thePacket,
      InteractionType type
    ) {
      this.world = world;
      this.player = player;
      this.targetBlock = targetBlock;
      this.targetDirection = targetDirection;
      this.thePacket = thePacket;
      this.type = type;
    }

    public PacketContainer thePacket() {
      return thePacket;
    }

    public InteractionType type() {
      return type;
    }

    public World world() {
      return world;
    }

    public Player player() {
      return player;
    }

    public void enter() {
      entered = true;
    }

    public boolean entered() {
      return entered;
    }
  }
}
