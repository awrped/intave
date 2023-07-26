package de.jpx3.intave.block.fluid.old;

import de.jpx3.intave.block.access.VolatileBlockAccess;
import de.jpx3.intave.block.variant.BlockVariantRegister;
import de.jpx3.intave.klass.rewrite.PatchyAutoTranslation;
import de.jpx3.intave.share.NativeVector;
import de.jpx3.intave.user.User;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.IBlockData;
import org.bukkit.Material;

import java.util.*;
import java.util.function.Function;

public final class DynamicFluidResolver extends FluidResolver {
  private static final Set<Material> fluids = EnumSet.noneOf(Material.class);
  private static final Map<Material, Set<Integer>> waterloggedVariants = new EnumMap<>(Material.class);
  private static final Map<Material, Function<Integer, Float>> levelToHeight = new EnumMap<>(Material.class);

  static {
    for (Material value : Material.values()) {
      if (value.isBlock()) {
        Set<Integer> variantSet = BlockVariantRegister.variantIdsOf(value);
        Map<Integer, Float> variantToHeight = new HashMap<>();
        for (Integer variantIndex : variantSet) {
          // aka IBlockData
          Object rawVariant = BlockVariantRegister.rawVariantOf(value, variantIndex);
          Fluid fluid = compileFrom(rawVariant);
          if (fluid.isEmpty()) {
            continue;
          }
          fluids.add(value);
          waterloggedVariants.computeIfAbsent(value, k -> new HashSet<>()).add(variantIndex);
          variantToHeight.put(variantIndex, fluid.height());
        }
        if (!variantToHeight.isEmpty()) {
          levelToHeight.put(value, variantIndex -> variantToHeight.getOrDefault(variantIndex, 0F));
        }
      }
    }
//    System.out.println(waterloggedVariants);
//    Integer id = waterloggedVariants.get(Material.getMaterial("WARPED_HANGING_SIGN")).iterator().next();
//    BlockVariantRegister.uncachedVariantOf(Material.getMaterial("WARPED_HANGING_SIGN"), id).dumpStates();
  }

  @PatchyAutoTranslation
  private static Fluid compileFrom(Object nativeVariant) {
    IBlockData variant = (IBlockData) nativeVariant;
    Block block = variant.getBlock();
//    block.c_(variant);
    return Fluid.empty();
  }

  @Override
  protected Fluid fluidAt(User user, int x, int y, int z) {
    Material type = VolatileBlockAccess.typeAccess(user, x, y, z);
    Set<Integer> waterloggedVariants = DynamicFluidResolver.waterloggedVariants.get(type);
    if (waterloggedVariants.isEmpty()) {
      return Fluid.empty();
    }
    int variantId = VolatileBlockAccess.variantIndexAccess(user, user.player().getWorld(), x, y, z);
    if (waterloggedVariants.contains(variantId)) {
      if (levelToHeight.get(type) != null) {
        return Fluid.of(FluidTag.WATER, true, levelToHeight.get(type).apply(variantId));
      }
      return Fluid.of(FluidTag.WATER, true, 1.0f);
    }
    return Fluid.empty();
  }

  @Override
  protected NativeVector flowVectorAt(User user, int x, int y, int z) {
    return NativeVector.ZERO;
  }
}
