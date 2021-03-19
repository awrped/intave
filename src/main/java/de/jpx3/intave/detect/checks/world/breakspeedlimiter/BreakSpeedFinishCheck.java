package de.jpx3.intave.detect.checks.world.breakspeedlimiter;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.detect.IntaveMetaCheckPart;
import de.jpx3.intave.detect.checks.world.BreakSpeedLimiter;
import de.jpx3.intave.event.packet.ListenerPriority;
import de.jpx3.intave.event.packet.PacketDescriptor;
import de.jpx3.intave.event.packet.PacketSubscription;
import de.jpx3.intave.event.packet.Sender;
import de.jpx3.intave.event.service.ViolationService;
import de.jpx3.intave.reflect.ReflectiveEntityAccess;
import de.jpx3.intave.tools.AccessHelper;
import de.jpx3.intave.user.*;
import de.jpx3.intave.world.block.BlockDataAccess;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class BreakSpeedFinishCheck extends IntaveMetaCheckPart<BreakSpeedLimiter, BreakSpeedFinishCheck.BreakSpeedFinishMeta> {
  public BreakSpeedFinishCheck(BreakSpeedLimiter parentCheck) {
    super(parentCheck, BreakSpeedFinishMeta.class);
  }

  @PacketSubscription(
    priority = ListenerPriority.LOW,
    packets = {
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "POSITION"),
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "POSITION_LOOK"),
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "LOOK"),
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "FLYING"),
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "VEHICLE_MOVE")
    }
  )
  public void tickUpdate(PacketEvent event) {
    Player player = event.getPlayer();
    User user = UserRepository.userOf(player);
    BreakSpeedFinishMeta meta = metaOf(user);

    UserMetaClientData clientData = user.meta().clientData();
    UserMetaInventoryData inventoryData = user.meta().inventoryData();

    if (meta.breakProcess) {
      ItemStack itemInHand = inventoryData.heldItem();
      BlockPosition blockPosition = meta.targetBlockPosition;

      float blockDamage = clientData.flyingPacketStream()
        ? BlockDataAccess.blockDamage(player, itemInHand, blockPosition)
        : resolveBlockDamageOnGround(player, itemInHand, blockPosition);
      meta.curBlockDamageMP += blockDamage;
      meta.maximumBlockDamage = Math.max(meta.maximumBlockDamage, blockDamage);
    }
  }

  @PacketSubscription(
    priority = ListenerPriority.LOWEST,
    packets = {
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "BLOCK_DIG")
    }
  )
  public void receiveBlockAction(PacketEvent event) {
    Player player = event.getPlayer();
    User user = UserRepository.userOf(player);
    BreakSpeedFinishMeta meta = metaOf(user);
    UserMetaClientData clientData = user.meta().clientData();
    UserMetaInventoryData inventoryData = user.meta().inventoryData();

    ItemStack heldItem = inventoryData.heldItem();
    PacketContainer packet = event.getPacket();
    BlockPosition blockPosition = packet.getBlockPositionModifier().read(0);
    EnumWrappers.PlayerDigType digType = packet.getPlayerDigTypes().read(0);

    switch (digType) {
      case START_DESTROY_BLOCK: {
        float blockDamage = BlockDataAccess.blockDamage(player, heldItem, blockPosition);
        meta.breakProcess = true;
        meta.breakProcessStartTime = AccessHelper.now();
        meta.curBlockDamageMP = blockDamage;
        meta.targetBlockPosition = blockPosition;
        meta.maximumBlockDamage = blockDamage;
        break;
      }
      case STOP_DESTROY_BLOCK: {
        if (clientData.flyingPacketStream()) {
          float blockDamageDealt = meta.curBlockDamageMP;
          if (blockDamageDealt < 1) {
            String message = "finished breaking-process too quickly";
            String percentage = (int)(blockDamageDealt * 100d) + "%";
            String details = "at " + percentage;

            ViolationService violationService = IntavePlugin.singletonInstance().violationProcessor();
            violationService.processViolation(player, 10, "BreakSpeedLimiter", message, details);
          }
        } else {
          long milliseconds = resolveMillisecondsOf(meta.maximumBlockDamage);
          long receivedMilliseconds = AccessHelper.now() - meta.breakProcessStartTime;
          long exceeded = milliseconds - receivedMilliseconds;

          if (exceeded > 100) {
            String message = "finished breaking-process too quickly";
            String details = exceeded + "ms faster than expected";
            ViolationService violationService = IntavePlugin.singletonInstance().violationProcessor();
            violationService.processViolation(player, 1, "BreakSpeedLimiter", message, details);
          }
        }

        meta.curBlockDamageMP = 0f;
        meta.targetBlockPosition = null;
        meta.breakProcess = false;
        meta.maximumBlockDamage = Float.MIN_VALUE;
        break;
      }
    }
  }

  private long resolveMillisecondsOf(float blockDamage) {
    if (blockDamage == 0) {
      return 0;
    }
    long time = 0;
    float curBlockDamageMP = 0f;
    int iterationCountdown = 100;
    while (curBlockDamageMP < 1f) {
      curBlockDamageMP += blockDamage;
      time += 50;
      if (--iterationCountdown < 0) {
        break;
      }
    }
    return time;
  }

  private float resolveBlockDamageOnGround(
    Player player,
    ItemStack itemInHand,
    BlockPosition blockPosition
  ) {
    boolean onGroundBefore = ReflectiveEntityAccess.onGround(player);
    ReflectiveEntityAccess.setOnGround(player, true);
    float blockDamage = BlockDataAccess.blockDamage(player, itemInHand, blockPosition);
    ReflectiveEntityAccess.setOnGround(player, onGroundBefore);
    return blockDamage;
  }

  public static final class BreakSpeedFinishMeta extends UserCustomCheckMeta {
    private BlockPosition targetBlockPosition;
    private float curBlockDamageMP = 0f;
    private float maximumBlockDamage;
    private boolean breakProcess;
    private long breakProcessStartTime;
  }
}