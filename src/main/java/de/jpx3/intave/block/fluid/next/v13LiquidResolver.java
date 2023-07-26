package de.jpx3.intave.block.fluid.next;

import de.jpx3.intave.block.variant.BlockVariantRegister;
import de.jpx3.intave.klass.rewrite.PatchyAutoTranslation;
import net.minecraft.server.v1_13_R2.Fluid;
import net.minecraft.server.v1_13_R2.IBlockData;
import net.minecraft.server.v1_13_R2.TagsFluid;
import org.bukkit.Material;

@PatchyAutoTranslation
final class v13LiquidResolver implements LiquidResolver {
  @Override
  @PatchyAutoTranslation
  public Liquid liquidFrom(Material type, int variantIndex) {
    IBlockData blockData = (IBlockData) BlockVariantRegister.rawVariantOf(type, variantIndex);
    if (blockData == null) {
      return Dry.of();
    }
    Fluid fluid = blockData.s();
    if (fluid == null) {
      return Dry.of();
    }
    boolean dry = fluid.e();
    boolean isWater = fluid.a(TagsFluid.WATER);
    boolean isLava = fluid.a(TagsFluid.LAVA);
    boolean source = fluid.d();
    float height = fluid.getHeight();
    return select(isWater, isLava, dry, height, source);
  }
}
