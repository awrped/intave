package de.jpx3.intave.user.meta;

import com.comphenix.protocol.utility.MinecraftVersion;
import de.jpx3.intave.adapter.MinecraftVersions;
import de.jpx3.intave.adapter.ViaVersionAdapter;
import de.jpx3.intave.annotate.Relocate;
import de.jpx3.intave.user.User;
import de.jpx3.intave.version.ProtocolVersionConverter;
import org.bukkit.entity.Player;

@Relocate
public final class ProtocolMetadata {
  // final has been removed to disguise modified integer VERSION_DETAILS
  public static int VER_1_17 = 755; // 1.17
  public static int VER_1_16 = 735; // 1.16
  public static int VER_1_15 = 573; // 1.15
  public static int VER_1_14 = 477; // 1.14
  public static int VER_1_13_2 = 404; // 1.13.2
  public static int VER_1_13 = 393; // 1.13
  public static int VER_1_12 = 335; // 1.12
  public static int VER_1_11_1 = 316;
  public static int VER_1_11 = 315;
  public static int VER_1_10 = 210;
  public static int VER_1_9 = 107; // 1.9
  public static int VERSION_DETAILS = 97; // secret integer for security - DO NOT MODIFY
  public static int VER_1_8 = 47; // 1.8

  public static int VER_INVALID = 1000;

  private MinecraftVersion minecraftVersion;
  private String versionString;
  private String clientBrand = "Unknown";
  private int protocolVersion;
  private final User user;
  private int refreshes;

  public ProtocolMetadata(Player player, User user) {
    this.user = user;
    this.refresh(player);
  }

  public void refresh(Player player) {
    this.protocolVersion = player == null ? -1 : ViaVersionAdapter.protocolVersionOf(player);
    this.versionString = versionAsString();
    this.refreshes++;

    if (protocolVersion <= 0) {
      protocolVersion = VER_INVALID;
      minecraftVersion = MinecraftVersions.VER1_18_2;
    } else {
      minecraftVersion = new MinecraftVersion(versionString);
    }
  }

  private String versionAsString() {
    return ProtocolVersionConverter.versionByProtocolVersion(protocolVersion);
  }

  public int protocolVersion() {
    return protocolVersion;
  }

  public boolean legacyTeleportAccept() {
    return protocolVersion <= VER_1_8;
  }

  public float cameraSneakOffset() {
    boolean override = user.customClientSupport().isLegacySneakHeight();
    if (protocolVersion >= VER_1_13_2 && !override) {
      return 0.35f;
    } else {
      return 0.08f;
    }
  }

  @Deprecated
  public float hitBoxHeightWhenSneaking() {
    if (protocolVersion >= VER_1_13_2) {
      return 1.5F;
    } else if (protocolVersion >= VER_1_9) {
      return 1.65F;
    }
    return 1.8F;
  }

  public String clientBrand() {
    return clientBrand;
  }

  public void setClientBrand(String clientBrand) {
    this.clientBrand = clientBrand;
  }

  public boolean flyingPacketStream() {
    return protocolVersion <= VER_1_8 && !clientVersionOlderThanServerVersion();
  }

  public boolean supportsInventoryAchievementPacket() {
    return protocolVersion <= VER_1_11_1 && !clientVersionOlderThanServerVersion();
  }

  public boolean applyModernCollider() {
    return protocolVersion >= VER_1_14;
  }

  public boolean swimmingMechanics() {
    return protocolVersion >= VER_1_13;
  }

  public boolean canUseElytra() {
    return protocolVersion >= VER_1_9;
  }

  public boolean affectedByLevitation() {
    return protocolVersion >= VER_1_12;
  }

  public boolean roundEnvironmentNumbers() {
    return protocolVersion < VER_1_14;
  }

  public boolean sprintWhenSneaking() {
    return protocolVersion >= VER_1_14;
  }

  public boolean sprintWhenHandActive() {
    return protocolVersion >= VER_1_9;
  }

  public boolean delayedSneak() {
    return protocolVersion >= VER_1_15;
  }

  public boolean alternativeSneak() {
    return protocolVersion < VER_1_15 && protocolVersion >= VER_1_14;
  }

  public boolean motionResetOnCollision() {
    return protocolVersion < VER_1_14;
  }

  public boolean cavesAndCliffsUpdate() {
    return protocolVersion >= VER_1_17;
  }

  public boolean beeUpdate() {
    return protocolVersion >= VER_1_15;
  }

  public boolean waterUpdate() {
    return protocolVersion >= VER_1_13;
  }

  public boolean combatUpdate() {
    return protocolVersion >= VER_1_9;
  }

  public boolean oppositeBlockVectorBehavior() {
    return protocolVersion >= VER_1_14;
  }

  private Boolean behind;

  public boolean clientVersionOlderThanServerVersion() {
    if (behind == null || refreshes < 2) {
      MinecraftVersion server = MinecraftVersion.getCurrentVersion();
      MinecraftVersion client = new MinecraftVersion(versionAsString());
      behind = !client.isAtLeast(server);
    }
    return behind;
  }

  public String versionString() {
    return versionString;
  }

  public MinecraftVersion minecraftVersion() {
    return minecraftVersion;
  }
}