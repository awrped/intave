package de.jpx3.intave.detect.checks.movement.physics.water;

import de.jpx3.intave.tools.wrapper.*;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserMetaMovementData;
import de.jpx3.intave.world.BlockAccessor;
import de.jpx3.intave.world.BlockLiquidHelper;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

public final class WaterMovementLegacyResolver {
  public static boolean handleMaterialAcceleration(User user, WrappedAxisAlignedBB boundingBox) {
    Player player = user.player();
    UserMetaMovementData movementData = user.meta().movementData();
    int minX = WrappedMathHelper.floor(boundingBox.minX);
    int minY = WrappedMathHelper.floor(boundingBox.minY);
    int minZ = WrappedMathHelper.floor(boundingBox.minZ);
    int maxX = WrappedMathHelper.floor(boundingBox.maxX + 1.0D);
    int maxY = WrappedMathHelper.floor(boundingBox.maxY + 1.0D);
    int maxZ = WrappedMathHelper.floor(boundingBox.maxZ + 1.0D);
    boolean inWater = false;
    WrappedVector flowVector = null;

    for (int x = minX; x < maxX; ++x) {
      for (int y = minY; y < maxY; ++y) {
        for (int z = minZ; z < maxZ; ++z) {
          Block block = BlockAccessor.blockAccess(player.getWorld(), x, y, z);
          if (BlockLiquidHelper.isWater(block.getType())) {
            int level = resolveLiquidLevel(block);
            double d0 = (float) (y + 1) - resolveLiquidHeightPercentage(level);
            if ((double) maxY >= d0) {
              inWater = true;
              WrappedBlockPosition blockPosition = new WrappedBlockPosition(x, y, z);
              flowVector = resolveWaterFlowVector(player.getWorld(), blockPosition);
            }
          }
        }
      }
      if (flowVector != null && flowVector.lengthVector() > 0.0D) {
        flowVector = flowVector.normalize();
        double d1 = 0.014D;
        movementData.physicsLastMotionX += flowVector.xCoord * d1;
        movementData.physicsLastMotionY += flowVector.yCoord * d1;
        movementData.physicsLastMotionZ += flowVector.zCoord * d1;
        movementData.pastPushedByWaterFlow = 0;
      }
    }
    return inWater;
  }

  public static WrappedVector resolveWaterFlowVector(
    World world,
    WrappedBlockPosition pos
  ) {
    WrappedVector vec3 = new WrappedVector(0.0D, 0.0D, 0.0D);
    int flowDecay = resolveEffectiveFlowDecay(world, pos);

    for (WrappedEnumDirection enumDirection : WrappedEnumDirection.Plane.HORIZONTAL) {
      WrappedBlockPosition blockPosition = pos.offset(enumDirection);
      Location location = new Location(world, blockPosition.xCoord, blockPosition.yCoord, blockPosition.zCoord);
      int effectiveFlowDecay = resolveEffectiveFlowDecay(world, blockPosition);
      if (effectiveFlowDecay < 0) {
        if (!BlockLiquidHelper.isLiquid(BlockAccessor.blockAccess(location).getType())) {
//        if (!worldIn.getBlockState(blockPosition).getBlock().getMaterial().blocksMovement()) {
          effectiveFlowDecay = resolveEffectiveFlowDecay(world, blockPosition.down());
          if (effectiveFlowDecay >= 0) {
            int k = effectiveFlowDecay - (flowDecay - 8);
            vec3 = vec3.addVector(
              (blockPosition.xCoord - pos.xCoord) * k,
              (blockPosition.yCoord - pos.yCoord) * k,
              (blockPosition.zCoord - pos.zCoord) * k
            );
          }
        }
      } else {
        int l = effectiveFlowDecay - flowDecay;
        vec3 = vec3.addVector(
          (blockPosition.xCoord - pos.xCoord) * l,
          (blockPosition.yCoord - pos.yCoord) * l,
          (blockPosition.zCoord - pos.zCoord) * l
        );
      }
    }

    if (resolveLevel(world, pos) >= 8) {
      for (WrappedEnumDirection enumDirection : WrappedEnumDirection.Plane.HORIZONTAL) {
        WrappedBlockPosition blockPosition = pos.offset(enumDirection);
        Location location = new Location(world, blockPosition.xCoord, blockPosition.yCoord, blockPosition.zCoord);
        switch (enumDirection) {
          case NORTH: {
            location.add(0, 0, -1);
            break;
          }
          case EAST: {
            location.add(1, 0, 0);
            break;
          }
          case SOUTH: {
            location.add(0, 0, 1);
            break;
          }
          case WEST: {
            location.add(-1, 0, 0);
            break;
          }
        }
        if (BlockAccessor.blockAccess(location).getType().isSolid() || BlockAccessor.blockAccess(location.add(0.0, 1.0, 0.0)).getType().isSolid()) {
          vec3 = vec3.normalize().addVector(0.0D, -6.0D, 0.0D);
          break;
        }
      }
    }

    return vec3.normalize();
  }

  private static int resolveLevel(World world, WrappedBlockPosition pos) {
    Location location = new Location(world, pos.xCoord, pos.yCoord, pos.zCoord);
    Block blockAccess = BlockAccessor.blockAccess(location);
    if (!BlockLiquidHelper.isWater(blockAccess.getType())) {
      return -1;
    }
    return resolveLiquidLevel(blockAccess);
  }

  public static float resolveLiquidHeightPercentage(int meta) {
    if (meta >= 8) {
      meta = 0;
    }
    return (float) (meta + 1) / 9.0F;
  }

  public static int resolveLiquidLevel(Block block) {
    BlockState state = block.getState();
    //noinspection deprecation
    return state.getData().getData();
  }

  public static int resolveEffectiveFlowDecay(World world, WrappedBlockPosition pos) {
    int i = resolveLevel(world, pos);
    return i >= 8 ? 0 : i;
  }
}