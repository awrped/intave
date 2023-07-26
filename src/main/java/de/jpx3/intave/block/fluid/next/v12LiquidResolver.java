package de.jpx3.intave.block.fluid.next;

import de.jpx3.intave.block.variant.BlockVariantRegister;
import org.bukkit.Material;

final class v12LiquidResolver implements LiquidResolver {
  private static final Material STATIONARY_WATER = Material.getMaterial("STATIONARY_WATER");
  private static final Material STATIONARY_LAVA = Material.getMaterial("STATIONARY_LAVA");

  @Override
  public Liquid liquidFrom(Material type, int variantIndex) {
    if (type == STATIONARY_WATER) {
      return StationaryWater.of();
    } else if (type == STATIONARY_LAVA) {
      return StationaryLava.of();
    }
    boolean isWater = type == Material.WATER;
    boolean isLava = type == Material.LAVA;
    if (!isWater && !isLava) {
      return Dry.of();
    }
    float height = liquidHeightFromLevel(levelOfLiquidAt(type, variantIndex));
    if (isWater) {
      return FlowingWater.ofHeight(height);
    } else {
      return FlowingLava.ofHeight(height);
    }
  }

  private static int levelOfLiquidAt(Material material, int variantIndex) {
    return BlockVariantRegister
      .variantOf(material, variantIndex)
      .propertyOf("level");
  }

  private static float liquidHeightFromLevel(int level) {
    if (level >= 8) {
      level = 0;
    }
    return (float) (level + 1) / 9.0F;
  }
}
