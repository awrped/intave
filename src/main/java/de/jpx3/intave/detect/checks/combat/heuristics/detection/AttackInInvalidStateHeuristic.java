package de.jpx3.intave.detect.checks.combat.heuristics.detection;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.jpx3.intave.detect.IntaveCheckPart;
import de.jpx3.intave.detect.checks.combat.Heuristics;
import de.jpx3.intave.detect.checks.combat.heuristics.Anomaly;
import de.jpx3.intave.detect.checks.combat.heuristics.Confidence;
import de.jpx3.intave.event.entity.WrappedEntity;
import de.jpx3.intave.event.packet.ListenerPriority;
import de.jpx3.intave.event.packet.PacketDescriptor;
import de.jpx3.intave.event.packet.PacketSubscription;
import de.jpx3.intave.event.packet.Sender;
import de.jpx3.intave.user.*;
import org.bukkit.entity.Player;

import static de.jpx3.intave.user.UserMetaClientData.PROTOCOL_VERSION_BOUNTIFUL_UPDATE;

public final class AttackInInvalidStateHeuristic extends IntaveCheckPart<Heuristics> {
  public AttackInInvalidStateHeuristic(Heuristics heuristics) {
    super(heuristics);
  }

  @PacketSubscription(
    priority = ListenerPriority.HIGH,
    packets = {
      @PacketDescriptor(sender = Sender.CLIENT, packetName = "USE_ENTITY")
    }
  )
  public void receiveAttack(PacketEvent event) {
    Player player = event.getPlayer();
    PacketContainer packet = event.getPacket();
    checkGUIScreen(player);
    checkDeadEntity(player, packet);
  }

  private void checkGUIScreen(Player player) {
    User user = userOf(player);
    UserMetaClientData clientData = user.meta().clientData();
    UserMetaAbilityData abilityData = user.meta().abilityData();
    float health = abilityData.health;
    if (health <= 0f) {
      int ticksAgo = abilityData.ticksToLastHealthUpdate;
      String description = "attacked in gui screen (version " + clientData.versionString() + ") | " + ticksAgo;
      Anomaly anomaly = Anomaly.anomalyOf("161", Confidence.NONE, Anomaly.Type.KILLAURA, description);
      parentCheck().saveAnomaly(player, anomaly);
    }
  }

  private void checkDeadEntity(Player player, PacketContainer packet) {
    User user = UserRepository.userOf(player);
    UserMetaAttackData attackData = user.meta().attackData();
    UserMetaClientData clientData = user.meta().clientData();
    WrappedEntity entity = attackData.lastAttackedEntity();
    if (entity == null || !entity.clientSynchronized || !entity.isEntityLiving) {
      return;
    }
    if (clientData.protocolVersion() != PROTOCOL_VERSION_BOUNTIFUL_UPDATE) {
      return;
    }
    EnumWrappers.EntityUseAction action = packet.getEntityUseActions().read(0);
    if (action == EnumWrappers.EntityUseAction.ATTACK && entity.dead) {
      String description = "attacked a dead entity " + entity.entityName();
      Anomaly anomaly = Anomaly.anomalyOf("161", Confidence.NONE, Anomaly.Type.KILLAURA, description);
      parentCheck().saveAnomaly(player, anomaly);
    }
  }
}