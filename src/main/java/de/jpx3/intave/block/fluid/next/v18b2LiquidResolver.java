package de.jpx3.intave.block.fluid.next;

import de.jpx3.intave.block.variant.BlockVariantRegister;
import de.jpx3.intave.klass.Lookup;
import de.jpx3.intave.klass.locate.MethodSearchBySignature;
import de.jpx3.intave.klass.rewrite.PatchyAutoTranslation;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.material.Fluid;
import org.bukkit.Material;

import java.lang.invoke.MethodHandle;

@PatchyAutoTranslation
final class v18b2LiquidResolver implements LiquidResolver {
  private static Object TAG_KEY_WATER = null;
  private static Object TAG_KEY_LAVA = null;

  private static final MethodHandle resolveTagKey;

  static {
    try {
      TAG_KEY_WATER = Lookup.serverField("TagsFluid", "WATER").get(null);
      TAG_KEY_LAVA = Lookup.serverField("TagsFluid", "LAVA").get(null);
    } catch (IllegalAccessException exception) {
      exception.printStackTrace();
    }
    resolveTagKey = MethodSearchBySignature
      .ofClass(Lookup.serverClass("Fluid"))
      .withParameters(new Class[]{Lookup.serverClass("TagKey")})
      .withReturnType(Boolean.TYPE).search().findFirstOrThrow();
  }

  @Override
  @PatchyAutoTranslation
  public Liquid liquidFrom(Material type, int variantIndex) {
    IBlockData blockData = (IBlockData) BlockVariantRegister.rawVariantOf(type, variantIndex);
    if (blockData == null) {
      return Dry.of();
    }
    try {
      Fluid fluid = blockData.getFluid();
      boolean dry = fluid.isEmpty();
      boolean isWater = (boolean) resolveTagKey.invoke(fluid, TAG_KEY_WATER);
      boolean isLava = (boolean) resolveTagKey.invoke(fluid, TAG_KEY_WATER);
      boolean source = fluid.isSource();
      float height = fluid.d();
      return select(isWater, isLava, dry, height, source);
    } catch (Throwable throwable) {
      throwable.printStackTrace();
      return Dry.of();
    }
  }
}
