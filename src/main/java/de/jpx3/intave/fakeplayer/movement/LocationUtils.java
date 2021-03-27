package de.jpx3.intave.fakeplayer.movement;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public final class LocationUtils {
  private final static double MAX_RELATIVE_MOVE_DIST = 3.5;

  public static boolean needTeleport(Location location1, Location location2) {
    if (location1.getWorld() != location2.getWorld()) {
      return true;
    }
    boolean exceedDistance = distanceBetweenLocations(location1, location2) > MAX_RELATIVE_MOVE_DIST;
    if (exceedDistance) {
      return true;
    }
    double xDist = Math.abs(location1.getX() - location2.getX());
    double yDist = Math.abs(location1.getY() - location2.getY());
    double zDist = Math.abs(location2.getZ() - location2.getZ());
    return xDist > MAX_RELATIVE_MOVE_DIST || yDist > MAX_RELATIVE_MOVE_DIST || zDist > MAX_RELATIVE_MOVE_DIST;
  }

  public static double distanceBetweenLocations(Location location1, Location location2) {
    if (location1.getWorld() != location2.getWorld()) {
      return 0.0;
    }
    return location1.distance(location2);
  }

  public static boolean equalRotations(Location location1, Location location2) {
    boolean equalYaw = location1.getYaw() == location2.getYaw();
    boolean equalPitch = location1.getPitch() == location2.getPitch();
    return equalYaw && equalPitch;
  }

  public static Location locationBehind(Location location, double distance) {
    location = location.clone();
    Vector direction = CameraUtils.getDirection(location.getYaw(), 0.0f);
    location.add(direction.multiply(-distance));
    return location;
  }
}