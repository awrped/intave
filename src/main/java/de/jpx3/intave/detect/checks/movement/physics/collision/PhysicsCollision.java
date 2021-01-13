package de.jpx3.intave.detect.checks.movement.physics.collision;

import com.comphenix.protocol.utility.MinecraftVersion;
import de.jpx3.intave.tools.annotate.Nullable;
import de.jpx3.intave.user.User;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.List;

public abstract class PhysicsCollision {
  public void setup(MinecraftVersion serverVersion) {
  }

  @Nullable
  public Vector entityCollidedWithBlock(
    User user,
    Location location, Location from,
    double motionX, double motionY, double motionZ
  ) {
    return null;
  }

  @Nullable
  public Vector entityCollidedWithBlock(User user, double motionX, double motionY, double motionZ) {
    return null;
  }

  @Nullable
  public Vector landed(User user, double motionX, double motionY, double motionZ) {
    return null;
  }

  @Nullable
  public Vector speedFactor(User user, double motionX, double motionY, double motionZ) {
    return null;
  }

  public void fallenUpon(User user) {
  }

  public boolean supportedOnServerVersion() {
    return true;
  }

  public abstract List<Material> materials();
}