package de.jpx3.intave.detect.checks.world.placementanalysis;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.detect.IntaveCheckPart;
import de.jpx3.intave.detect.checks.world.PlacementAnalysis;
import de.jpx3.intave.event.packet.PacketDescriptor;
import de.jpx3.intave.event.packet.PacketSubscription;
import de.jpx3.intave.event.packet.Sender;
import de.jpx3.intave.event.punishment.AttackNerfStrategy;
import de.jpx3.intave.event.service.violation.Violation;
import de.jpx3.intave.user.User;
import org.bukkit.entity.Player;

import static de.jpx3.intave.detect.checks.world.PlacementAnalysis.COMMON_FLAG_MESSAGE;

public final class PlacementFacingAnalyzer extends IntaveCheckPart<PlacementAnalysis> {
  private final IntavePlugin plugin;

  public PlacementFacingAnalyzer(PlacementAnalysis parentCheck) {
    super(parentCheck);
    this.plugin = IntavePlugin.singletonInstance();
  }

  @PacketSubscription(
    packets = {
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "BLOCK_PLACE")
    }
  )
  public void checkPlacementVector(PacketEvent event) {
    Player player = event.getPlayer();
    User user = userOf(player);
    PacketContainer packet = event.getPacket();
    if (blockingPlacementPacket(packet)) {
      return;
    }
    StructureModifier<Float> floatStructureModifier = packet.getFloat();
    if(floatStructureModifier.size() < 3) {
      return;
    }
    float f1 = floatStructureModifier.read(0);
    float f2 = floatStructureModifier.read(1);
    float f3 = floatStructureModifier.read(2);
    if (f1 < 0 || f2 < 0 || f3 < 0 || f1 > 1 || f2 > 1 || f3 > 1) {
      Violation violation = Violation.builderFor(PlacementAnalysis.class)
        .withPlayer(player)
        .withMessage(COMMON_FLAG_MESSAGE)
        .withVL(5)
        .build();
      plugin.violationProcessor().processViolation(violation);
      user.applyAttackNerfer(AttackNerfStrategy.CANCEL_FIRST_HIT);
      user.applyAttackNerfer(AttackNerfStrategy.HT_MEDIUM);
    }
  }

  private boolean blockingPlacementPacket(PacketContainer packet) {
    Integer integer = packet.getIntegers().readSafely(0);
    return integer != null && integer == 255;
  }
}
