package de.jpx3.intave.fakeplayer;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.fakeplayer.movement.LocationUtils;
import de.jpx3.intave.fakeplayer.movement.types.Movement;
import de.jpx3.intave.fakeplayer.randomaction.ActionType;
import de.jpx3.intave.fakeplayer.randomaction.RandomAction;
import de.jpx3.intave.fakeplayer.randomaction.actions.EquipmentAction;
import de.jpx3.intave.fakeplayer.randomaction.actions.HurtAnimationAction;
import de.jpx3.intave.fakeplayer.randomaction.actions.SwingAnimationAction;
import de.jpx3.intave.tools.wrapper.WrappedMathHelper;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserMetaAttackData;
import de.jpx3.intave.user.UserRepository;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class FakePlayer implements TickTaskScheduler {
  public final static float SPAWN_HEALTH_STATE = 20.0f;
  private final static IntavePlugin plugin = IntavePlugin.singletonInstance();
  private final static ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

  private final WrappedDataWatcher wrappedDataWatcher = new WrappedDataWatcher();
  private final Set<RandomAction> actions;
  private final Movement movement;
  private final Player parentPlayer;
  private final WrappedGameProfile wrappedGameProfile;
  private final String tabListPrefix, prefix;
  private final int fakePlayerID, timeout;
  public double killAuraVL = 0;
  private int taskId;
  private int previousLatency = 0, ticks = 0;

  FakePlayer(
    Movement movement,
    Player parentPlayer,
    WrappedGameProfile wrappedGameProfile,
    String tabListPrefix,
    String prefix,
    int entityId,
    int timeout
  ) {
    User user = UserRepository.userOf(parentPlayer);
    user.meta().attackData().setFakePlayer(this);
    this.timeout = timeout;
    this.movement = movement;
    this.wrappedGameProfile = wrappedGameProfile;
    this.parentPlayer = parentPlayer;
    this.fakePlayerID = entityId;
    this.tabListPrefix = tabListPrefix;
    this.prefix = prefix;
    this.actions = ImmutableSet.of(
      new SwingAnimationAction(parentPlayer, this),
      new HurtAnimationAction(parentPlayer, this),
      new EquipmentAction(parentPlayer, this)
    );
  }

  @Override
  public void startTickScheduler() {
    IntavePlugin plugin = IntavePlugin.singletonInstance();
    this.taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::onTick, 0, 1);
  }

  public void spawn(Location location) {
    Preconditions.checkNotNull(location);
    this.movement.location = location;
    this.movement.botDistance = this.movement.hidePartBotDistance = location.distance(parentPlayer.getLocation());
    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.NAMED_ENTITY_SPAWN);
    packet.getModifier().writeSafely(0, fakePlayerID);
    packet.getModifier().writeSafely(1, wrappedGameProfile.getUUID());
    packet.getModifier().writeSafely(2, WrappedMathHelper.floor(location.getX() * 32.0));
    packet.getModifier().writeSafely(3, WrappedMathHelper.floor(location.getY() * 32.0));
    packet.getModifier().writeSafely(4, WrappedMathHelper.floor(location.getZ() * 32.0));
    packet.getModifier().writeSafely(5, FakeEntityPositionHelper.getFixRotation(location.getYaw()));
    packet.getModifier().writeSafely(6, FakeEntityPositionHelper.getFixRotation(location.getPitch()));
    packet.getModifier().writeSafely(7, 0);
    // Entity
    wrappedDataWatcher.setObject(0, (byte) 0);
    wrappedDataWatcher.setObject(1, (short) 300);
    wrappedDataWatcher.setObject(3, (byte) 0);
    wrappedDataWatcher.setObject(2, "");
    wrappedDataWatcher.setObject(4, (byte) 0);
    // EntityLivingBase
    wrappedDataWatcher.setObject(7, 0);
    wrappedDataWatcher.setObject(8, (byte) 0);
    wrappedDataWatcher.setObject(9, (byte) 0);
    wrappedDataWatcher.setObject(6, 1.0F); // health
    // EntityPlayer
    wrappedDataWatcher.setObject(16, (byte) 0);
    wrappedDataWatcher.setObject(17, 0.0F);
    wrappedDataWatcher.setObject(18, 0);
    wrappedDataWatcher.setObject(10, (byte) 0);
    packet.getDataWatcherModifier().writeSafely(0, wrappedDataWatcher);
    String tabListName = tabListPrefix + wrappedGameProfile.getName();
    TabListHelper.addToTabList(this.parentPlayer, this.wrappedGameProfile, tabListName);
    try {
      protocolManager.sendServerPacket(this.parentPlayer, packet);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    RandomAction.findAndProcessAction(this.actions, ActionType.EQUIPMENT);
    FakePlayerMetaDataHelper.updateHealthFor(parentPlayer, this, SPAWN_HEALTH_STATE);
    applyDisplayName();
    startTickScheduler();
    sendLatency(0);
  }

  public void registerParentPlayerVelocity(double motionX, double motionY, double motionZ) {
    this.movement.velocityChanged = true;
    this.movement.velocityX = motionX * 4;
    this.movement.velocityY = motionY;
    this.movement.velocityZ = motionZ * 4;
  }

  private void applyDisplayName() {
    PacketContainer scoreboardCreatePacket = protocolManager.createPacket(PacketType.Play.Server.SCOREBOARD_TEAM);
    String teamName = PlayerNameHelper.randomString();
    scoreboardCreatePacket.getStrings().writeSafely(0, teamName);
    scoreboardCreatePacket.getStrings().writeSafely(2, prefix);
    try {
      protocolManager.sendServerPacket(this.parentPlayer, scoreboardCreatePacket);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    FakePlayerNameHelper.sendScoreboard(parentPlayer, teamName, wrappedGameProfile);
  }

  public void updateLatency() {
    int latency = nextLatency();
    sendLatency(latency);
  }

  private void sendLatency(int latency) {
    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
    WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromText(prefix);
    PlayerInfoData playerInfoData = new PlayerInfoData(
      wrappedGameProfile,
      latency,
      EnumWrappers.NativeGameMode.SURVIVAL,
      wrappedChatComponent
    );
    List<PlayerInfoData> playerInformationList = packet.getPlayerInfoDataLists().readSafely(0);
    playerInformationList.add(playerInfoData);
    packet.getPlayerInfoAction().writeSafely(0, EnumWrappers.PlayerInfoAction.UPDATE_LATENCY);
    packet.getPlayerInfoDataLists().writeSafely(0, playerInformationList);
    packet.getBooleans().writeSafely(0, true);
    try {
      protocolManager.sendServerPacket(this.parentPlayer, packet);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  private final static int LATENCY_BOUND = 25;

  private int nextLatency() {
    if (previousLatency == 0) {
      int latency = ThreadLocalRandom.current().nextInt(20, 250);
      this.previousLatency = latency;
      return latency;
    }
    int boundingLatency = ThreadLocalRandom.current().nextInt(previousLatency - LATENCY_BOUND,
                                                              previousLatency + LATENCY_BOUND);
    int nextLatency = Math.max(LATENCY_BOUND, boundingLatency);
    previousLatency = nextLatency;
    return nextLatency;
  }

  public void despawn() {
    stopTickScheduler();
    TabListHelper.removeFromTabList(this.parentPlayer, this.wrappedGameProfile);
    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
    packet.getIntegerArrays().writeSafely(0, new int[]{this.fakePlayerID});
    try {
      protocolManager.sendServerPacket(this.parentPlayer, packet);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  private void onTick() {
    ticks++;
    processRandomAction();
    processMovement();
    decreaseViolationLevel();
    double distanceMoved = movement.distanceMoved();
    double distanceToPlayer = movement.distanceToPlayer(parentPlayer);
    setSprinting(distanceMoved > 0.0 && !this.movement.sneaking);
    if (distanceMoved < 0.5 && distanceToPlayer < 9) {
      if (System.currentTimeMillis() % 50 == 0L) {
        setSneaking(true);
      }
    } else {
      setSneaking(false);
    }
    if (this.ticks % 10 == 0 && this.movement.onGround) {
      sendWalkingSoundEffect(this.movement.location);
    }
  }

  private final static int SOUND_CONVERT_FACTOR = 8;
  private final static String SOUND_NAME = "step.stone";

  private void sendWalkingSoundEffect(Location location) {
    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.NAMED_SOUND_EFFECT);
    packet.getStrings().writeSafely(0, SOUND_NAME);
    packet.getSoundCategories().writeSafely(0, null);
    int effectPosX = (int) (location.getX() * SOUND_CONVERT_FACTOR);
    int effectPosY = (int) (location.getY() * SOUND_CONVERT_FACTOR);
    int effectPosZ = (int) (location.getZ() * SOUND_CONVERT_FACTOR);
    packet.getIntegers().writeSafely(0, effectPosX);
    packet.getIntegers().writeSafely(1, effectPosY);
    packet.getIntegers().writeSafely(2, effectPosZ);
    float volume = (System.currentTimeMillis() % 5 == 0L ? 1.0f : 0.15f);
    packet.getFloat().writeSafely(0, volume);
    try {
      protocolManager.sendServerPacket(parentPlayer, packet);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  private boolean shouldDespawn() {
    return false;
//    return System.currentTimeMillis() - user.lastEntityAttack > timeout;
  }

  private void decreaseViolationLevel() {
    if (killAuraVL > 0) {
      killAuraVL -= 0.1;
    }
  }

  private void processRandomAction() {
    for (RandomAction action : this.actions) {
      action.mayProcess();
    }
  }

  private void processMovement() {
    this.movement.applyMovementAndRotation(this.parentPlayer.getLocation());
    Location location = this.movement.location;
    Location prevLocation = this.movement.prevLocation;
    if (prevLocation != null) {
      boolean shouldTeleport = LocationUtils.needTeleport(location, prevLocation);
      boolean onGround = this.movement.onGround;
      if (shouldTeleport) {
        sendTeleport(location, onGround);
      } else {
        sendRelativeMovement(location, prevLocation, onGround);
      }
    }
  }

  private void sendRelativeMovement(
    Location to,
    Location from,
    boolean onGround
  ) {
    boolean move = LocationUtils.distanceBetweenLocations(to, from) != 0;
    boolean look = !LocationUtils.equalRotations(to, from);
    if (move && look) {
      PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.REL_ENTITY_MOVE_LOOK);
      packet.getIntegers().writeSafely(0, this.fakePlayerID);
      packet.getBytes().writeSafely(0, FakeEntityPositionHelper.relativeMoveDiff(to.getX(), from.getX()));
      packet.getBytes().writeSafely(1, FakeEntityPositionHelper.relativeMoveDiff(to.getY(), from.getY()));
      packet.getBytes().writeSafely(2, FakeEntityPositionHelper.relativeMoveDiff(to.getZ(), from.getZ()));
      packet.getBytes().writeSafely(3, FakeEntityPositionHelper.getFixRotation(to.getYaw()));
      packet.getBytes().writeSafely(4, FakeEntityPositionHelper.getFixRotation(to.getPitch()));
      packet.getBooleans().writeSafely(0, onGround);
      try {
        protocolManager.sendServerPacket(this.parentPlayer, packet);
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    } else if (move) {
      PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.REL_ENTITY_MOVE);
      packet.getIntegers().writeSafely(0, this.fakePlayerID);
      packet.getBytes().writeSafely(0, FakeEntityPositionHelper.relativeMoveDiff(to.getX(), from.getX()));
      packet.getBytes().writeSafely(1, FakeEntityPositionHelper.relativeMoveDiff(to.getY(), from.getY()));
      packet.getBytes().writeSafely(2, FakeEntityPositionHelper.relativeMoveDiff(to.getZ(), from.getZ()));
      packet.getBooleans().writeSafely(0, onGround);
      try {
        protocolManager.sendServerPacket(this.parentPlayer, packet);
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    } else if (look) {
      PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_LOOK);
      packet.getIntegers().writeSafely(0, this.fakePlayerID);
      packet.getBytes().writeSafely(0, FakeEntityPositionHelper.getFixRotation(to.getYaw()));
      packet.getBytes().writeSafely(1, FakeEntityPositionHelper.getFixRotation(to.getPitch()));
      packet.getBooleans().writeSafely(0, onGround);
      try {
        protocolManager.sendServerPacket(this.parentPlayer, packet);
      } catch (InvocationTargetException e) {
        e.printStackTrace();
      }
    }
    if (look) {
      headRotation(to.getYaw(), to.getPitch());
    }

    plugin.eventService()
      .transactionFeedbackService()
      .requestPong(parentPlayer, to, (player, target) -> {
        User user = UserRepository.userOf(player);
        UserMetaAttackData attackData = user.meta().attackData();
        attackData.fakePlayerLastReportedX = target.getX();
        attackData.fakePlayerLastReportedY = target.getY();
        attackData.fakePlayerLastReportedZ = target.getZ();
      });
  }

  public void sendTeleport(Location to, boolean onGround) {
    Preconditions.checkNotNull(to);
    float rotationYaw = to.getYaw();
    float rotationPitch = to.getPitch();
    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_TELEPORT);
    packet.getIntegers().writeSafely(0, this.fakePlayerID);
    packet.getIntegers().writeSafely(1, FakeEntityPositionHelper.getFixCoordinate(to.getX()));
    packet.getIntegers().writeSafely(2, FakeEntityPositionHelper.getFixCoordinate(to.getY()));
    packet.getIntegers().writeSafely(3, FakeEntityPositionHelper.getFixCoordinate(to.getZ()));
    packet.getBytes().writeSafely(0, FakeEntityPositionHelper.getFixRotation(rotationYaw));
    packet.getBytes().writeSafely(1, FakeEntityPositionHelper.getFixRotation(rotationPitch));
    packet.getBooleans().writeSafely(0, onGround);
    try {
      protocolManager.sendServerPacket(this.parentPlayer, packet);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    headRotation(rotationYaw, rotationPitch);
    movement.registerTeleport(to);
    plugin.eventService()
      .transactionFeedbackService()
      .requestPong(parentPlayer, to, (player, target) -> {
        User user = UserRepository.userOf(player);
        UserMetaAttackData attackData = user.meta().attackData();
        attackData.fakePlayerLastReportedX = target.getX();
        attackData.fakePlayerLastReportedY = target.getY();
        attackData.fakePlayerLastReportedZ = target.getZ();
      });
  }

  public void headRotation(float yaw, float pitch) {
    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
    packet.getIntegers().writeSafely(0, this.fakePlayerID);
    packet.getBytes().writeSafely(0, FakeEntityPositionHelper.getFixRotation(yaw));
    packet.getBytes().writeSafely(1, FakeEntityPositionHelper.getFixRotation(pitch));
    try {
      protocolManager.sendServerPacket(this.parentPlayer, packet);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  public void setSprinting(boolean sprinting) {
    FakePlayerMetaDataHelper.setSprinting(parentPlayer, this, sprinting);
    this.movement.sprinting = sprinting;
  }

  public void setSneaking(boolean sneaking) {
    FakePlayerMetaDataHelper.setSneaking(parentPlayer, this, sneaking);
    this.movement.sneaking = sneaking;
  }

  public void onAttack() {
    this.movement.combatEvent();
  }

  public int fakePlayerEntityId() {
    return this.fakePlayerID;
  }

  public Set<RandomAction> actions() {
    return this.actions;
  }

  public WrappedDataWatcher wrappedDataWatcher() {
    return wrappedDataWatcher;
  }

  public Movement movement() {
    return this.movement;
  }

  @Override
  public int taskId() {
    return this.taskId;
  }
}