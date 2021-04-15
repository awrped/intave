package de.jpx3.intave.detect.checks.combat.heuristics.detection;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.jpx3.intave.detect.IntaveCheckPart;
import de.jpx3.intave.detect.checks.combat.Heuristics;
import de.jpx3.intave.detect.checks.combat.heuristics.Anomaly;
import de.jpx3.intave.detect.checks.combat.heuristics.Confidence;
import de.jpx3.intave.event.packet.ListenerPriority;
import de.jpx3.intave.event.packet.PacketDescriptor;
import de.jpx3.intave.event.packet.PacketSubscription;
import de.jpx3.intave.event.packet.Sender;
import de.jpx3.intave.event.service.entity.WrappedEntity;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserMetaAttackData;
import de.jpx3.intave.user.UserMetaClientData;
import de.jpx3.intave.user.UserRepository;
import org.bukkit.entity.Player;

import static de.jpx3.intave.user.UserMetaClientData.PROTOCOL_VERSION_BOUNTIFUL_UPDATE;

public final class AttackDeadEntityHeuristic extends IntaveCheckPart<Heuristics> {
  public AttackDeadEntityHeuristic(Heuristics parentCheck) {
    super(parentCheck);
  }

  @PacketSubscription(
    priority = ListenerPriority.HIGH,
    packets = {
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "USE_ENTITY")
    }
  )
  public void receiveAttack(PacketEvent event) {
    Player player = event.getPlayer();
    User user = UserRepository.userOf(player);
    UserMetaAttackData attackData = user.meta().attackData();
    UserMetaClientData clientData = user.meta().clientData();
    WrappedEntity entity = attackData.lastAttackedEntity();
    if (entity == null || !entity.clientSynchronized) {
      return;
    }
    if (clientData.protocolVersion() != PROTOCOL_VERSION_BOUNTIFUL_UPDATE) {
      return;
    }
    PacketContainer packet = event.getPacket();
    EnumWrappers.EntityUseAction action = packet.getEntityUseActions().read(0);
    if (action == EnumWrappers.EntityUseAction.ATTACK && entity.dead) {
      Anomaly anomaly = Anomaly.anomalyOf("161", Confidence.NONE, Anomaly.Type.KILLAURA, "attacked a dead entity");
      parentCheck().saveAnomaly(player, anomaly);
    }
  }
}