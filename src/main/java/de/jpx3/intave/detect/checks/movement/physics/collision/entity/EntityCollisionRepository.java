package de.jpx3.intave.detect.checks.movement.physics.collision.entity;

import de.jpx3.intave.detect.checks.movement.Physics;
import de.jpx3.intave.detect.checks.movement.physics.collision.PhysicsEntityCollision;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserMetaClientData;

public final class EntityCollisionRepository {
  private PhysicsEntityCollision legacyCollisionResolver;
  private PhysicsEntityCollision newCollisionResolver;

  public EntityCollisionRepository() {
    setup();
  }

  private void setup() {
    legacyCollisionResolver = new EntityCollisionLegacyResolver();
    newCollisionResolver = new EntityCollisionNewResolver();
  }

  public EntityCollisionResult resolveEntityCollisionOf(
    User user, Physics.PhysicsProcessorContext context, boolean inWeb,
    double positionX, double positionY, double positionZ
  ) {
    UserMetaClientData clientData = user.meta().clientData();
//    return legacyCollisionResolver.resolveCollision(user, context, inWeb, positionX, positionY, positionZ);
    return clientData.applyNewEntityCollisions()
      ? newCollisionResolver.resolveCollision(user, context, inWeb, positionX, positionY, positionZ)
      : legacyCollisionResolver.resolveCollision(user, context, inWeb, positionX, positionY, positionZ);
  }
}