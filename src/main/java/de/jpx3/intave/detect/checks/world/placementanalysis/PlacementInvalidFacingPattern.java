package de.jpx3.intave.detect.checks.world.placementanalysis;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import de.jpx3.intave.detect.IntaveCheckPart;
import de.jpx3.intave.detect.checks.world.PlacementAnalysis;
import de.jpx3.intave.event.packet.PacketDescriptor;
import de.jpx3.intave.event.packet.PacketSubscription;
import de.jpx3.intave.event.packet.Sender;
import org.bukkit.entity.Player;

public final class PlacementInvalidFacingPattern extends IntaveCheckPart<PlacementAnalysis> {
  public PlacementInvalidFacingPattern(PlacementAnalysis parentCheck) {
    super(parentCheck);
  }

  @PacketSubscription(
    packets = {
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "BLOCK_PLACE")
    }
  )
  public void receivePlacementPacket(PacketEvent event) {
    Player player = event.getPlayer();
    PacketContainer packet = event.getPacket();
    if (packet.getFloat().size() < 3) {
      return;
    }
    float f1 = packet.getFloat().read(0);
    float f2 = packet.getFloat().read(1);
    float f3 = packet.getFloat().read(2);
    if (f1 < 0 || f2 < 0 || f3 < 0 || f1 > 1 || f2 > 1 || f3 > 1) {
      parentCheck().processViolation(player);
    }
  }
}