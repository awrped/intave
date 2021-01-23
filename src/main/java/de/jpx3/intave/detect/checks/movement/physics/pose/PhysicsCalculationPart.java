package de.jpx3.intave.detect.checks.movement.physics.pose;

import de.jpx3.intave.detect.checks.movement.Physics;
import de.jpx3.intave.detect.checks.movement.physics.collision.block.BlockCollisionRepository;
import de.jpx3.intave.detect.checks.movement.physics.collision.entity.EntityCollisionRepository;
import de.jpx3.intave.detect.checks.movement.physics.collision.entity.EntityCollisionResult;
import de.jpx3.intave.detect.checks.movement.physics.water.AquaticWaterMovementBase;
import de.jpx3.intave.user.User;

public abstract class PhysicsCalculationPart {
  private EntityCollisionRepository entityCollisionRepository;
  private BlockCollisionRepository blockCollisionRepository;
  private AquaticWaterMovementBase aquaticWaterMovementBase;

  public final void setup(
    EntityCollisionRepository entityCollisionRepository,
    BlockCollisionRepository blockCollisionRepository,
    AquaticWaterMovementBase aquaticWaterMovementBase
  ) {
    this.entityCollisionRepository = entityCollisionRepository;
    this.blockCollisionRepository = blockCollisionRepository;
    this.aquaticWaterMovementBase = aquaticWaterMovementBase;
  }

  public abstract EntityCollisionResult performSimulation(
    User user, Physics.PhysicsProcessorContext context,
    float yawSine, float yawCosine, float friction,
    float keyForward, float keyStrafe,
    boolean sneaking, boolean attackReduce,
    boolean jumped, boolean sprinting,
    boolean handActive
  );

  public abstract void prepareNextTick(
    User user,
    double positionX, double positionY, double positionZ,
    double motionX, double motionY, double motionZ
  );

  public EntityCollisionRepository entityCollisionRepository() {
    return entityCollisionRepository;
  }

  public AquaticWaterMovementBase aquaticWaterMovementBase() {
    return aquaticWaterMovementBase;
  }

  public BlockCollisionRepository blockCollisionRepository() {
    return blockCollisionRepository;
  }

  public boolean requiresKeyCalculation() {
    return true;
  }
}