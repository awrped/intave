package de.jpx3.intave.detect.checks.combat.heuristics.detection;

import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.adapter.ProtocolLibAdapter;
import de.jpx3.intave.detect.IntaveMetaCheckPart;
import de.jpx3.intave.detect.checks.combat.Heuristics;
import de.jpx3.intave.detect.checks.combat.heuristics.Anomaly;
import de.jpx3.intave.detect.checks.combat.heuristics.Confidence;
import de.jpx3.intave.event.packet.ListenerPriority;
import de.jpx3.intave.event.packet.PacketDescriptor;
import de.jpx3.intave.event.packet.PacketSubscription;
import de.jpx3.intave.event.packet.Sender;
import de.jpx3.intave.event.punishment.AttackNerfStrategy;
import de.jpx3.intave.event.service.entity.WrappedEntity;
import de.jpx3.intave.tools.MathHelper;
import de.jpx3.intave.tools.client.RotationHelper;
import de.jpx3.intave.tools.wrapper.WrappedMathHelper;
import de.jpx3.intave.user.*;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class RotationSnapHeuristic extends IntaveMetaCheckPart<Heuristics, RotationSnapHeuristic.RotationSnapHeuristicMeta> {
  private final IntavePlugin plugin;

  public RotationSnapHeuristic(Heuristics parentCheck) {
    super(parentCheck, RotationSnapHeuristicMeta.class);
    this.plugin = IntavePlugin.singletonInstance();
  }

  @PacketSubscription(
    priority = ListenerPriority.HIGH,
    packets = {
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "ARM_ANIMATION")
    }
  )
  public void receiveSwingPacket(PacketEvent event) {
    Player player = event.getPlayer();
    User user = userOf(player);
    RotationSnapHeuristicMeta meta = metaOf(user);

    meta.lastSwing = 0;
  }

  @PacketSubscription(
    priority = ListenerPriority.HIGH,
    packets = {
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "USE_ENTITY")
    }
  )
  public void receiveAttackPacket(PacketEvent event) {
    Player player = event.getPlayer();
    User user = userOf(player);
    RotationSnapHeuristicMeta meta = metaOf(user);

    EnumWrappers.EntityUseAction entityUseAction = event.getPacket().getEntityUseActions().read(0);

    if (entityUseAction == EnumWrappers.EntityUseAction.ATTACK) {
      meta.lastAttack = 0;
    }
  }

  @PacketSubscription(
    priority = ListenerPriority.HIGH,
    packets = {
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "POSITION_LOOK"),
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "LOOK"),
    }
  )
  public void receiveRotationPacket(PacketEvent event) {
    metaOf(userOf(event.getPlayer())).rotationPacketCounter++;
  }

  private double keysToRotation(int strafe, int forward) {
    return Math.toDegrees(Math.atan2(strafe, forward)) - 90;
  }

  @PacketSubscription(
    priority = ListenerPriority.HIGH,
    packets = {
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "POSITION_LOOK"),
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "LOOK"),
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "FLYING"),
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "POSITION")
    }
  )
  public void receiveMovementPacket(PacketEvent event) {
    if (ProtocolLibAdapter.serverVersion().isAtLeast(ProtocolLibAdapter.COMBAT_UPDATE)) {
      return;
    }
    Player player = event.getPlayer();
    User user = UserRepository.userOf(player);
    UserMetaMovementData movementData = user.meta().movementData();
    RotationSnapHeuristicMeta meta = metaOf(user);
    double yawMotion = Math.abs(movementData.lastRotationYaw - movementData.rotationYaw);
    UserMetaAttackData attackData = user.meta().attackData();
    double diffPerfectYaw = Math.abs(WrappedMathHelper.wrapAngleTo180_double(attackData.perfectYaw() - movementData.rotationYaw));

    if(yawMotion > 40 && meta.yawMotions[1] < 9) {
      if(meta.lastKeyStrafe != movementData.keyStrafe || meta.lastKeyForward != movementData.keyForward) {
        double directionLast = movementData.rotationYaw + keysToRotation(meta.lastKeyStrafe, meta.lastKeyForward);
        double direction = movementData.lastRotationYaw + keysToRotation(movementData.keyStrafe, movementData.keyForward);

        direction = Math.floorMod((int) direction, 360);
        directionLast = Math.floorMod((int) directionLast, 360);

//      String key = resolveKeysFromInput(movementData.keyForward, movementData.keyStrafe);
//      String lastKey = resolveKeysFromInput(meta.lastKeyForward, meta.lastKeyStrafe);
        boolean silentMovement = (int) (WrappedMathHelper.wrapAngleTo180_double(directionLast - direction) / 45d) == 0;
        meta.silentMovements[0] = silentMovement;
      }

      if(attackData.lastAttackedEntity() != null) {
        WrappedEntity wrappedEntity = attackData.lastAttackedEntity();
        WrappedEntity.EntityPositionContext lastEntityPosition = wrappedEntity.positionHistory.get(Math.max(wrappedEntity.positionHistory.size() - 2, 0));
        float lastPerfectYaw = RotationHelper.resolveYawRotation(lastEntityPosition, movementData.lastPositionX, movementData.lastPositionZ);
        double lastDiff = Math.abs(WrappedMathHelper.wrapAngleTo180_double(lastPerfectYaw - movementData.lastRotationYaw));
        meta.perfectRotations[1] = lastDiff;

        meta.perfectRotations[0] = diffPerfectYaw;
      }
    }

    boolean isLegit = meta.yawMotions[1] > 9 || meta.yawMotions[0] < 40 || yawMotion > 9;

    if (!isLegit && (meta.lastSwing <= 3 || meta.lastAttack <= 3) && meta.rotationPacketCounter > 10 && movementData.lastTeleport > 7) {
      double valueOfSnap = meta.yawMotions[0];
      String description = "rotation snap ["
        +  MathHelper.formatDouble(meta.yawMotions[1], 2)
        + "/" +  MathHelper.formatDouble(meta.yawMotions[0], 2)
        + "/" + MathHelper.formatDouble(yawMotion, 2) + "]"

        + " s:" + Math.min(meta.lastSwing, 9)
        + "/" + Math.min(meta.lastAttack, 9);

      int addVL = 0;
      if(valueOfSnap > 90 && meta.lastAttack <= 3) {
        addVL = 15;
      }
      boolean hadSilentMovement = meta.silentMovements[1];
      if(hadSilentMovement) {
        description += " SM";
        if(valueOfSnap > 90) {
          addVL = 40;
        } else {
          addVL = 20;
        }
      }

      if(attackData.lastAttackedEntity() != null) {
        double minValue = Math.min(meta.perfectRotations[0], meta.perfectRotations[1]);
        double maxValue = Math.max(meta.perfectRotations[0], meta.perfectRotations[1]);

        if(maxValue == Double.POSITIVE_INFINITY) {
          minValue = Math.min(meta.perfectRotations[1], meta.perfectRotations[2]);
          maxValue = Math.max(meta.perfectRotations[1], meta.perfectRotations[2]);
        }

        if(maxValue != Double.POSITIVE_INFINITY) {
          if(minValue < 10 && maxValue > 50) {
            if(valueOfSnap > 360) {
              addVL += 80;
            } else if(valueOfSnap > 65) {
              addVL += 20;
            } else {
              addVL += 10;
            }
            description += " pYaw:"
              + MathHelper.formatDouble(minValue, 2)
              + "/" + MathHelper.formatDouble(maxValue, 2);
          }
        }
      }

      if(valueOfSnap >= 178) {
        addVL = Math.max(addVL, 50);
      }

      if(addVL >= 40) {
        plugin.eventService().combatMitigator().mitigate(user, AttackNerfStrategy.HT_MEDIUM);
      }
      Confidence confidence = Confidence.confidenceFrom(addVL + meta.internalViolation);
      meta.internalViolation += addVL;
      meta.internalViolation -= confidence.level();
      description += " conf:" + confidence.level();

      if(addVL > 5) {
        int options = Anomaly.AnomalyOption.DELAY_128s;
        Anomaly anomaly = Anomaly.anomalyOf("102", confidence, Anomaly.Type.KILLAURA, description, options);
        parentCheck().saveAnomaly(player, anomaly);
      }
    }

    prepareNextTick(meta, yawMotion, user);
  }

  private void prepareNextTick(RotationSnapHeuristicMeta meta, double yawMotion, User user) {
    UserMetaMovementData movementData = user.meta().movementData();
    meta.lastKeyForward = movementData.keyForward;
    meta.lastKeyStrafe = movementData.keyStrafe;

    meta.yawMotions[1] = meta.yawMotions[0];
    meta.yawMotions[0] = yawMotion;

    meta.perfectRotations[2] = meta.perfectRotations[1];
    meta.perfectRotations[1] = meta.perfectRotations[0];
    meta.perfectRotations[0] = Double.POSITIVE_INFINITY;

    meta.silentMovements[1] = meta.silentMovements[0];
    meta.silentMovements[0] = false;

    meta.lastSwing++;
    meta.lastAttack++;
  }


  public static final class RotationSnapHeuristicMeta extends UserCustomCheckMeta {
    private double[] yawMotions = new double[2];
    private double[] perfectRotations = new double[3];
    private boolean[] silentMovements = new boolean[2];
    private int internalViolation;
    private int lastKeyForward;
    private int lastKeyStrafe;
    // used to disable the check on startup
    private int rotationPacketCounter;
    private int lastSwing;
    private int lastAttack;
  }
}
