package de.jpx3.intave.detect.checks.movement.physics.water.aquatics;

import de.jpx3.intave.detect.checks.movement.physics.water.AquaticWaterMovementBase;
import de.jpx3.intave.detect.checks.movement.physics.water.WaterMovementLegacyResolver;
import de.jpx3.intave.tools.wrapper.WrappedAxisAlignedBB;
import de.jpx3.intave.tools.wrapper.WrappedBlockPosition;
import de.jpx3.intave.tools.wrapper.WrappedMathHelper;
import de.jpx3.intave.tools.wrapper.WrappedVector;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserMetaMovementData;
import de.jpx3.intave.world.BlockAccessor;
import de.jpx3.intave.world.BlockLiquidHelper;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import static de.jpx3.intave.detect.checks.movement.physics.water.WaterMovementLegacyResolver.resolveLiquidLevel;

public final class AquaticUnknownMovementResolver extends AquaticWaterMovementBase {
  @Override
  public boolean fluidStateEmpty(User user, double x, double y, double z) {
    World world = user.player().getWorld();
    return !isWaterAt(world, x, y, z);
  }

  @Override
  public boolean handleFluidAcceleration(User user, WrappedAxisAlignedBB boundingBox) {
    Player player = user.player();
    UserMetaMovementData movementData = user.meta().movementData();
    World world = player.getWorld();
    WrappedAxisAlignedBB wrappedAxisAlignedBB = boundingBox.shrink(0.001D);
    int minX = WrappedMathHelper.floor(wrappedAxisAlignedBB.minX);
    int minY = WrappedMathHelper.floor(wrappedAxisAlignedBB.minY);
    int minZ = WrappedMathHelper.floor(wrappedAxisAlignedBB.minZ);
    int maxX = WrappedMathHelper.ceil(wrappedAxisAlignedBB.maxX);
    int maxY = WrappedMathHelper.ceil(wrappedAxisAlignedBB.maxY);
    int maxZ = WrappedMathHelper.ceil(wrappedAxisAlignedBB.maxZ);
    boolean inWater = false;
    WrappedVector waterFlowTotal = WrappedVector.ZERO;
    double d0 = 0;
    double countedWaterCollisions = 0;

    for (int x = minX; x < maxX; ++x) {
      for (int y = minY; y < maxY; ++y) {
        for (int z = minZ; z < maxZ; ++z) {
          Block block = BlockAccessor.blockAccess(player.getWorld(), x, y, z);
          if (BlockLiquidHelper.isWater(block.getType())) {
            int fluidHeight = resolveLiquidLevel(block);
            double d1 = (float) y + fluidHeight;
            if ((double) maxY >= wrappedAxisAlignedBB.minY) {
              inWater = true;
              d0 = Math.max(d1 - wrappedAxisAlignedBB.minY, d0);
              WrappedBlockPosition blockPosition = new WrappedBlockPosition(x, y, z);
              WrappedVector flowVector = WaterMovementLegacyResolver.resolveWaterFlowVector(world, blockPosition);
              if (d0 < 0.4) {
                flowVector = flowVector.scale(d0);
              }
              waterFlowTotal = waterFlowTotal.add(flowVector);
              ++countedWaterCollisions;
            }
          }
        }
      }
      if (waterFlowTotal.length() > 0.0D) {
        if (countedWaterCollisions > 0) {
          waterFlowTotal = waterFlowTotal.scale(1.0D / countedWaterCollisions);
        }
        waterFlowTotal = waterFlowTotal.normalize();
        double d2 = 0.014D;
        movementData.physicsLastMotionX += waterFlowTotal.xCoord * d2;
        movementData.physicsLastMotionY += waterFlowTotal.yCoord * d2;
        movementData.physicsLastMotionZ += waterFlowTotal.zCoord * d2;
        movementData.pastPushedByWaterFlow = 0;
      }
    }
    return inWater;
  }

  @Override
  public boolean areEyesInFluid(User user, double positionX, double positionY, double positionZ) {
    Player player = user.player();
    World world = player.getWorld();
    UserMetaMovementData movementData = user.meta().movementData();
    double eyeHeight = movementData.eyeHeight();
    positionY -= 1;
    double playerViewPositionY = positionY + eyeHeight;
    int blockX = WrappedMathHelper.floor(positionX);
    int blockPlayerViewPositionY = WrappedMathHelper.floor(playerViewPositionY);
    int blockZ = WrappedMathHelper.floor(positionZ);
    return isWaterAt(world, blockX, blockPlayerViewPositionY, blockZ);
  }

  private boolean isWaterAt(World world, double x, double y, double z) {
    return BlockLiquidHelper.isWater(BlockAccessor.blockAccess(world, x, y, z).getType());
  }

  @Override
  @Deprecated
  public Object blockPositionOf(int x, int y, int z) {
    return null;
  }

  @Override
  @Deprecated
  public boolean fluidTaggedWithWater(Object fluidState) {
    return false;
  }

  @Override
  @Deprecated
  public Object fluidState(User user, Object blockPosition) {
    return null;
  }

  @Override
  @Deprecated
  public float fluidHeight(Object fluidState) {
    return 0;
  }

  @Override
  @Deprecated
  public WrappedVector resolveFlowVector(Object fluidState, Object world, Object blockPosition) {
    return null;
  }
}