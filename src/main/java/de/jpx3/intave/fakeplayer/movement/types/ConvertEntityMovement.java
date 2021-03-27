package de.jpx3.intave.fakeplayer.movement.types;

import de.jpx3.intave.fakeplayer.movement.LocationUtils;
import org.bukkit.Location;

/**
 * This movement mode doesn't apply real gravity on the entity.
 */
public final class ConvertEntityMovement extends Movement {
  private final static double MOVE_MULTIPLIER = 0.91f;
  private boolean fallPart = false;

  @Override
  public void move(Location parentLocation) {
    Location expectedLocation = LocationUtils.locationBehind(parentLocation, this.botDistance);
    if (this.velocityChanged) {
      expectedLocation.add(this.velocityX, this.velocityY, this.velocityZ);
    } else {
      expectedLocation.add(this.velocityX, 0.0, this.velocityZ);
    }
    if (this.onGround && this.fallPart) {
      this.fallPart = false;
    }
    this.motionX = expectedLocation.getX() - this.location.getX();
    this.motionY = expectedLocation.getY() - this.location.getY();
    this.motionZ = expectedLocation.getZ() - this.location.getZ();
    this.velocityX *= MOVE_MULTIPLIER;
    this.velocityZ *= MOVE_MULTIPLIER;
  }

  @Override
  public double minBotDistance() {
    return 4.0;
  }

  @Override
  public boolean doBlockCollisions() {
    return true;
  }
}