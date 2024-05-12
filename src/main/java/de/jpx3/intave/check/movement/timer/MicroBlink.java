package de.jpx3.intave.check.movement.timer;

import com.comphenix.protocol.events.PacketEvent;
import de.jpx3.intave.annotate.DispatchTarget;
import de.jpx3.intave.check.MetaCheckPart;
import de.jpx3.intave.check.movement.Timer;
import de.jpx3.intave.math.Histogram;
import de.jpx3.intave.math.MathHelper;
import de.jpx3.intave.module.Modules;
import de.jpx3.intave.module.linker.packet.PacketSubscription;
import de.jpx3.intave.module.mitigate.AttackNerfStrategy;
import de.jpx3.intave.module.violation.Violation;
import de.jpx3.intave.packet.reader.EntityUseReader;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.meta.CheckCustomMetadata;
import de.jpx3.intave.user.meta.MovementMetadata;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;

import static com.comphenix.protocol.wrappers.EnumWrappers.EntityUseAction.ATTACK;
import static de.jpx3.intave.module.linker.packet.PacketId.Client.USE_ENTITY;

public class MicroBlink extends MetaCheckPart<Timer, MicroBlink.MicroBlinkMeta> {
  public MicroBlink(Timer parentCheck) {
    super(parentCheck, MicroBlinkMeta.class);
  }

  @PacketSubscription(
    packetsIn = USE_ENTITY
  )
  public void receiveUseEntity(
    User user, EntityUseReader reader, Cancellable cancellable
  ) {
    if (reader.useAction() == ATTACK) {
      MicroBlinkMeta meta = metaOf(user);
      meta.lastAttack = System.currentTimeMillis();
    }
  }

  @DispatchTarget
  public void receiveMovement(PacketEvent event) {
    User user = userOf(event.getPlayer());
    MicroBlinkMeta meta = metaOf(user);
    MovementMetadata movement = user.meta().movement();
    double horizontalDistance = movement.motion().horizontalLength();

    Histogram timeHistogram = meta.timeHistogram;

    if (horizontalDistance > 0.125 && meta.lastHorizontalDistance > 0.125) {
      long timeDifference = System.currentTimeMillis() - meta.lastMovement;
      timeHistogram.add(timeDifference);
      double probability = timeHistogram.normalProbability(timeDifference);

      long pastAttack = System.currentTimeMillis() - meta.lastAttack;
      if (probability < 0.000001 && timeDifference > 150 && timeDifference < 400 && pastAttack < 1250 && movement.lastTeleport > 5) {
        if (++meta.violationLevel > 5) {
          Violation violation = Violation.builderFor(Timer.class)
            .forPlayer(user.player())
            .withCustomThreshold("microblink")
            .withMessage("seems to be purposefully micro-lagging in combat")
            .withDetails(MathHelper.formatDouble(probability * 100, 6) + "% likelihood of " + timeDifference + "ms")
            .withVL(meta.violationLevel - 5)
            .build();
          Modules.violationProcessor().processViolation(violation);
          if (meta.violationLevel > 10) {
            meta.violationLevel = 10;
          }
        }
      } else {
        meta.violationLevel = Math.max(0, meta.violationLevel - 0.007);
      }
    }

    meta.lastMovement = System.currentTimeMillis();
    meta.lastHorizontalDistance = horizontalDistance;
  }

  public static class MicroBlinkMeta extends CheckCustomMetadata {
    private long lastMovement = 0L;
    private double lastHorizontalDistance = 0.0;
    private final Histogram timeHistogram = new Histogram(0, 500, 10, 20 * 60 * 2);
    private double violationLevel = 0.0;

    private long lastAttack = 0L;
  }
}
