package de.jpx3.intave.block.fluid.next;

import de.jpx3.intave.block.variant.BlockVariantRegister;
import de.jpx3.intave.klass.rewrite.PatchyAutoTranslation;
import net.minecraft.server.v1_16_R3.Fluid;
import net.minecraft.server.v1_16_R3.IBlockData;
import net.minecraft.server.v1_16_R3.TagsFluid;
import org.bukkit.Material;

@PatchyAutoTranslation
final class v16LiquidResolver implements LiquidResolver {
  @Override
  @PatchyAutoTranslation
  public Liquid liquidFrom(Material type, int variantIndex) {
    IBlockData blockData = (IBlockData) BlockVariantRegister.rawVariantOf(type, variantIndex);
    if (blockData == null) {
      return Dry.of();
    }
    Fluid fluid = blockData.getFluid();
    if (fluid == null) {
      return Dry.of();
    }
    boolean dry = fluid.isEmpty();
    boolean isWater = fluid.a(TagsFluid.WATER);
    boolean isLava = fluid.a(TagsFluid.LAVA);
    boolean source = fluid.isSource();
    float height = fluid.e() / 9f;
    return select(isWater, isLava, dry, height, source);
  }
}

