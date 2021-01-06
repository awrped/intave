package de.jpx3.intave.adapter;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.utility.MinecraftVersion;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.jpx3.intave.access.InvalidDependencyException;

import java.util.Arrays;

public final class ProtocolLibAdapter {
  public static final MinecraftVersion NETHER_UPDATE = new MinecraftVersion("1.16");
  public static final MinecraftVersion BEE_UPDATE = new MinecraftVersion("1.15");
  public static final MinecraftVersion VILLAGE_UPDATE = new MinecraftVersion("1.14");
  public static final MinecraftVersion AQUATIC_UPDATE = new MinecraftVersion("1.13");
  public static final MinecraftVersion COLOR_UPDATE = new MinecraftVersion("1.12");
  public static final MinecraftVersion EXPLORATION_UPDATE = new MinecraftVersion("1.11");
  public static final MinecraftVersion FROSTBURN_UPDATE = new MinecraftVersion("1.10");
  public static final MinecraftVersion COMBAT_UPDATE = new MinecraftVersion("1.9");
  public static final MinecraftVersion BOUNTIFUL_UPDATE = new MinecraftVersion("1.8");
  public static final MinecraftVersion SKIN_UPDATE = new MinecraftVersion("1.7.8");
  public static final MinecraftVersion WORLD_UPDATE = new MinecraftVersion("1.7.2");
  public static final MinecraftVersion HORSE_UPDATE = new MinecraftVersion("1.6.1");
  public static final MinecraftVersion REDSTONE_UPDATE = new MinecraftVersion("1.5.0");
  public static final MinecraftVersion SCARY_UPDATE = new MinecraftVersion("1.4.2");

  public static MinecraftVersion serverVersion() {
    return ProtocolLibrary.getProtocolManager().getMinecraftVersion();
  }

  public static void checkIfOutdated() {
    boolean temporaryPlayer = Arrays.stream(PacketEvent.class.getMethods()).anyMatch(method -> method.getName().equalsIgnoreCase("isPlayerTemporary"));
    boolean specifiedEnumModifier = Arrays.stream(EnumWrappers.class.getMethods()).anyMatch(method -> method.getName().equalsIgnoreCase("getGenericConverter") && method.getParameterCount() == 2);

    if(!specifiedEnumModifier) {
      throw new InvalidDependencyException("Your version of ProtocolLib is outdated");
    }
  }
}