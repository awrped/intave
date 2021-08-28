package de.jpx3.intave.world.border;

import org.bukkit.Location;
import org.bukkit.World;

public final class WorldBorders {
  private static WorldBorderAccess worldBorderAccess = new BukkitWorldBorderAccess();

  public static void setup() {
    worldBorderAccess = new BukkitWorldBorderAccess();
    worldBorderAccess = new CachedForwardingWorldBorderAccess(worldBorderAccess);
  }

  public static double sizeOfWorldBorderIn(World world) {
    return worldBorderAccess.sizeOf(world);
  }

  public static Location centerOfWorldBorderIn(World world) {
    return worldBorderAccess.centerOf(world);
  }
}
