package de.jpx3.intave.world.border;

import org.bukkit.Location;
import org.bukkit.World;

public final class BukkitWorldBorderAccess implements WorldBorderAccess {
  @Override
  public double sizeOf(World world) {
    return world.getWorldBorder().getSize();
  }

  @Override
  public Location centerOf(World world) {
    return world.getWorldBorder().getCenter();
  }
}
