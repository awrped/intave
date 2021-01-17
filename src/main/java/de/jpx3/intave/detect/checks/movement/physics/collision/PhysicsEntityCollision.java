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

    double x = context.motionX;
    double z = context.motionZ;

    while (x != 0.0D && Collision.hasNoCollisions(player, boundingBox.offset(x, -length, 0.0D))) {
      if (x < 0.05D && x >= -0.05D) {
        x = 0.0D;
      } else if (x > 0.0D) {
        x -= 0.05D;
      } else {
        x += 0.05D;
      }
    }

    while (z != 0.0D && Collision.hasNoCollisions(player, boundingBox.offset(0.0D, -length, z))) {
      if (z < 0.05D && z >= -0.05D) {
        z = 0.0D;
      } else if (z > 0.0D) {
        z -= 0.05D;
      } else {
        z += 0.05D;
      }
    }

    while (x != 0.0D && z != 0.0D && Collision.hasNoCollisions(player, boundingBox.offset(x, -length, z))) {
      if (x < 0.05D && x >= -0.05D) {
        x = 0.0D;
      } else if (x > 0.0D) {
        x -= 0.05D;
      } else {
        x += 0.05D;
      }

      if (z < 0.05D && z >= -0.05D) {
        z = 0.0D;
      } else if (z > 0.0D) {
        z -= 0.05D;
      } else {
        z += 0.05D;
      }
    }

    context.motionX = x;
    context.motionZ = z;
  }
}