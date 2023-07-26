package de.jpx3.intave.block.fluid.next;

import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.access.IntaveInternalException;
import de.jpx3.intave.block.variant.BlockVariant;
import de.jpx3.intave.block.variant.BlockVariantRegister;
import de.jpx3.intave.klass.rewrite.PatchyLoadingInjector;
import org.bukkit.Material;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static de.jpx3.intave.adapter.MinecraftVersions.*;

public class Liquids {
  private static LiquidResolver resolver;
  private final static Map<Material, Map<Integer, Liquid>> liquids = new HashMap<>();

  public static void setup() {
    String className;
    if (VER1_18_2.atOrAbove()) {
      className = "de.jpx3.intave.block.fluid.next.v18b2LiquidResolver";
    } else if (VER1_16_0.atOrAbove()) {
      className = "de.jpx3.intave.block.fluid.next.v16LiquidResolver";
    } else if (VER1_14_0.atOrAbove()) {
      className = "de.jpx3.intave.block.fluid.next.v14LiquidResolver";
    } else if (VER1_13_0.atOrAbove()) {
      className = "de.jpx3.intave.block.fluid.next.v13LiquidResolver";
    } else {
      className = "de.jpx3.intave.block.fluid.next.v12LiquidResolver";
    }
    PatchyLoadingInjector.loadUnloadedClassPatched(IntavePlugin.class.getClassLoader(), className);
    try {
      resolver = (LiquidResolver) Class.forName(className).newInstance();
    } catch (Exception exception) {
      throw new IntaveInternalException(exception);
    }
    int variantCount = 0;
    for (Material value : Material.values()) {
      if (value.isBlock()) {
        Map<Integer, Liquid> variants = new HashMap<>();
        for (int variantIndex : BlockVariantRegister.variantIdsOf(value)) {
          variantCount++;
          Liquid currentLiquid = resolver.liquidFrom(value, variantIndex);
          if (!currentLiquid.isDry()) {
            BlockVariant properties = BlockVariantRegister.uncachedVariantOf(value, variantIndex);
            String propertyString = "{"+properties.propertyNames().stream().map(s -> s + ": " + properties.propertyOf(s)).collect(Collectors.joining(", ")) +"}";

            System.out.println("Found liquid " + currentLiquid + " at " + value + ":" + propertyString);
          }
          variants.put(variantIndex, currentLiquid);
        }
        liquids.put(value, variants);
      }
    }
    System.out.println("Checked " + variantCount + " variants");
  }
}
