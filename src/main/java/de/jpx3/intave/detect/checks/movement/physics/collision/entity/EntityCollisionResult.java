package de.jpx3.intave.detect.checks.movement.physics.collision.entity;

import de.jpx3.intave.detect.checks.movement.Physics;

public final class EntityCollisionResult {
  private final Physics.PhysicsProcessorContext context;
  private final boolean onGround, collidedHorizontally, collidedVertically;
  private final boolean resetMotionX, resetMotionZ;

  public EntityCollisionResult(
    Physics.PhysicsProcessorContext context, boolean onGround,
    boolean collidedHorizontally, boolean collidedVertically,
    boolean resetMotionX, boolean resetMotionZ
  ) {
    if (context == null) {
      throw new IllegalArgumentException("Context cannot be null");
    }
    this.context = context;
    this.onGround = onGround;
    this.collidedHorizontally = collidedHorizontally;
    this.collidedVertically = collidedVertically;
    this.resetMotionX = resetMotionX;
    this.resetMotionZ = resetMotionZ;
  }

  public Physics.PhysicsProcessorContext context() {
    return context;
  }

  public boolean onGround() {
    return onGround;
  }

  public boolean collidedHorizontally() {
    return collidedHorizontally;
  }

  public boolean collidedVertically() {
    return collidedVertically;
  }

  public boolean resetMotionX() {
    return resetMotionX;
  }

  public boolean resetMotionZ() {
    return resetMotionZ;
  }
}