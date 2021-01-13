package de.jpx3.intave.detect.checks.movement.physics.collision;

import com.comphenix.protocol.utility.MinecraftVersion;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserMetaClientData;
import de.jpx3.intave.user.UserMetaMovementData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;

import static de.jpx3.intave.user.UserMetaClientData.PROTOCOL_VERSION_BEE_UPDATE;

final class PhysicsCollisionWeb extends PhysicsCollision {
  private List<Material> material;

  @Override
  public void setup(MinecraftVersion serverVersion) {
    Material legacyWeb = Material.getMaterial("WEB");
    Material web = legacyWeb != null ? legacyWeb : Material.getMaterial("COBWEB");
    material = Collections.singletonList(web);
  }

  @Override
  public Vector entityCollidedWithBlock(User user, Location location, Location from, double motionX, double motionY, double motionZ) {
    UserMetaMovementData movementData = user.meta().movementData();
    movementData.inWeb = true;
    movementData.artificialFallDistance = 0;

    UserMetaClientData clientData = user.meta().clientData();
    if (clientData.protocolVersion() >= PROTOCOL_VERSION_BEE_UPDATE) {
      return new Vector(motionX * 0.25, motionY * 0.05f, motionZ * 0.25);
    }
    return null;
  }


  @Override
  public List<Material> materials() {
    return material;
  }
}