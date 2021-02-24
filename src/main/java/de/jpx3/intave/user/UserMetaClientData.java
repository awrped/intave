package de.jpx3.intave.user;

import de.jpx3.intave.adapter.ViaVersionAdapter;
import org.bukkit.entity.Player;

public final class UserMetaClientData {
  public final static int PROTOCOL_VERSION_NETHER_UPDATE = 735; // 1.16
  public final static int PROTOCOL_VERSION_BEE_UPDATE = 573; // 1.15
  public final static int PROTOCOL_VERSION_VILLAGE_UPDATE = 477; // 1.14
  public final static int SOMETHING_BETWEEN = 404; // 1.13.2
  public final static int PROTOCOL_VERSION_AQUATIC_UPDATE = 393; // 1.13
  public final static int PROTOCOL_VERSION_COLOR_UPDATE = 335; // 1.12
  public final static int PROTOCOL_VERSION_COMBAT_UPDATE = 107; // 1.9
  public final static int PROTOCOL_VERSION_BOUNTIFUL_UPDATE = 47; // 1.8
  private final int protocolVersion;

  public UserMetaClientData(Player player) {
    this.protocolVersion = player == null ? -1 : ViaVersionAdapter.protocolVersionOf(player);
  }

  public int protocolVersion() {
    return protocolVersion;
  }

  public double cameraSneakOffset() {
    return protocolVersion >= SOMETHING_BETWEEN ? 0.35 : 0.08;
  }

  public boolean hitBoxSneakAffected() {
    return protocolVersion >= PROTOCOL_VERSION_COMBAT_UPDATE;
  }

  public boolean flyingPacketStream() {
    return protocolVersion <= PROTOCOL_VERSION_BOUNTIFUL_UPDATE;
  }

  public boolean inventoryAchievementPacket() {
    return protocolVersion <= PROTOCOL_VERSION_BOUNTIFUL_UPDATE;
  }

  public boolean applyNewEntityCollisions() {
    // >= 1.14
    return protocolVersion >= PROTOCOL_VERSION_VILLAGE_UPDATE;
  }

  public boolean sprintWhenSneaking() {
    // >= 1.14
    return protocolVersion >= PROTOCOL_VERSION_VILLAGE_UPDATE;
  }

  public boolean delayedSneak() {
    // 1.15
    return protocolVersion >= PROTOCOL_VERSION_BEE_UPDATE;
  }

  public boolean alternativeSneak() {
    // < 1.15 && >= 1.14
    return protocolVersion < PROTOCOL_VERSION_BEE_UPDATE && protocolVersion >= PROTOCOL_VERSION_VILLAGE_UPDATE;
  }

  public boolean motionResetOnCollision() {
    // 1.14
    return protocolVersion < PROTOCOL_VERSION_VILLAGE_UPDATE;
  }

  public boolean waterUpdate() {
    // >= 1.13
    return protocolVersion >= PROTOCOL_VERSION_AQUATIC_UPDATE;
  }
}