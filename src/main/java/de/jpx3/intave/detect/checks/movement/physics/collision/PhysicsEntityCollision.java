package de.jpx3.intave.detect.checks.movement.physics.collision;

import de.jpx3.intave.detect.checks.movement.Physics;
import de.jpx3.intave.detect.checks.movement.physics.collision.entity.EntityCollisionResult;
import de.jpx3.intave.tools.wrapper.WrappedAxisAlignedBB;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserMetaMovementData;
import de.jpx3.intave.world.collision.Collision;
import org.bukkit.entity.Player;

public interface PhysicsEntityCollision {
  float STEP_HEIGHT = 0.6f;

  EntityCollisionResult resolveCollision(
    User user, Physics.PhysicsProcessorContext context,
    boolean inWeb,
    double positionX, double positionY, double positionZ
  );

  default void calculateBackOffFromEdge(User user, double length, Physics.PhysicsProcessorContext context) {
    Player player = user.player();
    UserMetaMovementData movementData = user.meta().movementData();
    WrappedAxisAlignedBB boundingBox = movementData.boundingBox();

    double motionX = context.motionX;
    double motionZ = context.motionZ;

    while (motionX != 0.0D && Collision.hasNoCollisions(player, boundingBox.offset(motionX, -length, 0.0D))) {
      if (motionX < 0.05D && motionX >= -0.05D) {
        motionX = 0.0D;
      } else if (motionX > 0.0D) {
        motionX -= 0.05D;
      } else {
        motionX += 0.05D;
      }
    }

    while (motionZ != 0.0D && Collision.hasNoCollisions(player, boundingBox.offset(0.0D, -length, motionZ))) {
      if (motionZ < 0.05D && motionZ >= -0.05D) {
        motionZ = 0.0D;
      } else if (motionZ > 0.0D) {
        motionZ -= 0.05D;
      } else {
        motionZ += 0.05D;
      }
    }

    while (motionX != 0.0D && motionZ != 0.0D && Collision.hasNoCollisions(player, boundingBox.offset(motionX, -length, motionZ))) {
      if (motionX < 0.05D && motionX >= -0.05D) {
        motionX = 0.0D;
      } else if (motionX > 0.0D) {
        motionX -= 0.05D;
      } else {
        motionX += 0.05D;
      }

      if (motionZ < 0.05D && motionZ >= -0.05D) {
        motionZ = 0.0D;
      } else if (motionZ > 0.0D) {
        motionZ -= 0.05D;
      } else {
        motionZ += 0.05D;
      }
    }

    context.motionX = motionX;
    context.motionZ = motionZ;
  }
}