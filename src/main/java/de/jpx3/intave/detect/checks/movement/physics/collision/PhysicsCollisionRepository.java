package de.jpx3.intave.detect.checks.movement.physics.collision;

import com.comphenix.protocol.utility.MinecraftVersion;
import com.google.common.collect.Lists;
import de.jpx3.intave.adapter.ProtocolLibAdapter;
import de.jpx3.intave.tools.annotate.Nullable;
import de.jpx3.intave.user.User;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.List;

public final class PhysicsCollisionRepository {
  private final static MinecraftVersion MINECRAFT_VERSION = ProtocolLibAdapter.serverVersion();
  private final List<PhysicsCollision> blockCollisions = Lists.newArrayList();

  public PhysicsCollisionRepository() {
    try {
      initializeBlocks();
      setupBlocks();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void initializeBlocks() {
    blockCollisions.add(new PhysicsCollisionBed());
    blockCollisions.add(new PhysicsCollisionSlime());
    blockCollisions.add(new PhysicsCollisionWeb());
    blockCollisions.add(new PhysicsCollisionSoulSand());
    blockCollisions.add(new PhysicsCollisionBerryBush());
    blockCollisions.add(new PhysicsCollisionWeb());
  }

  private void setupBlocks() {
    for (PhysicsCollision blockCollision : blockCollisions) {
      blockCollision.setup(MINECRAFT_VERSION);
    }
  }

  @Nullable
  public Vector entityCollision(
    User user,
    Material material,
    Location location, Location from,
    double motionX, double motionY, double motionZ
  ) {
    PhysicsCollision collision = findPotentialCollision(material);
    return collision != null ? collision.entityCollidedWithBlock(user, location, from, motionX, motionY, motionZ) : null;
  }

  @Nullable
  public Vector entityCollision(
    User user,
    Material material,
    double motionX, double motionY, double motionZ
  ) {
    PhysicsCollision collision = findPotentialCollision(material);
    return collision != null ? collision.entityCollidedWithBlock(user, motionX, motionY, motionZ) : null;
  }

  @Nullable
  public Vector blockLanded(
    User user,
    Material material,
    double motionX, double motionY, double motionZ
  ) {
    PhysicsCollision collision = findPotentialCollision(material);
    return collision != null ? collision.landed(user, motionX, motionY, motionZ) : null;
  }

  @Nullable
  public Vector speedFactor(
    User user,
    Material material,
    double motionX, double motionY, double motionZ
  ) {
    PhysicsCollision collision = findPotentialCollision(material);
    return collision != null ? collision.speedFactor(user, motionX, motionY, motionZ) : null;
  }

  public void fallenUpon(User user, Material material) {
    PhysicsCollision collision = findPotentialCollision(material);
    if (collision != null) {
      collision.fallenUpon(user);
    }
  }

  private PhysicsCollision findPotentialCollision(Material material) {
    for (PhysicsCollision blockCollision : blockCollisions) {
      if (blockCollision.supportedOnServerVersion() && blockCollision.materials().contains(material)) {
        return blockCollision;
      }
    }
    return null;
  }
}