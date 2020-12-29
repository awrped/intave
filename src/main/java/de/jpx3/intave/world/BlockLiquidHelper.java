package de.jpx3.intave.world;

import de.jpx3.intave.tools.wrapper.WrappedAxisAlignedBB;
import de.jpx3.intave.tools.wrapper.WrappedMathHelper;
import org.bukkit.Material;
import org.bukkit.World;

public final class BlockLiquidHelper {
  private final static Material STATIONARY_WATER = Material.getMaterial("STATIONARY_WATER");
  private final static Material STATIONARY_LAVA = Material.getMaterial("STATIONARY_LAVA");

  public static boolean isLiquid(Material material) {
    return isLava(material) || isWater(material);
  }

  public static boolean isLava(Material material) {
    return (STATIONARY_LAVA != null && material == STATIONARY_LAVA) || material == Material.LAVA;
  }

  public static boolean isWater(Material material) {
    return (STATIONARY_WATER != null && material == STATIONARY_WATER) || material == Material.WATER;
  }

  public static boolean isLavaInBB(World world, WrappedAxisAlignedBB boundingBox) {
    int minX = WrappedMathHelper.floor(boundingBox.minX);
    int minY = WrappedMathHelper.floor(boundingBox.minY);
    int minZ = WrappedMathHelper.floor(boundingBox.minZ);
    int maxX = WrappedMathHelper.floor(boundingBox.maxX + 1.0D);
    int maxY = WrappedMathHelper.floor(boundingBox.maxY + 1.0D);
    int maxZ = WrappedMathHelper.floor(boundingBox.maxZ + 1.0D);
    for (int x = minX; x < maxX; ++x) {
      for (int y = minY; y < maxY; ++y) {
        for (int z = minZ; z < maxZ; ++z) {
          if (BlockLiquidHelper.isLava(BlockAccessor.blockAccess(world, x, y, z).getType())) {
            return true;
          }
        }
      }
    }
    return false;
  }
}