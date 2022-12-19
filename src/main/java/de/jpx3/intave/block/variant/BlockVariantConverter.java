package de.jpx3.intave.block.variant;

import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.adapter.MinecraftVersions;
import de.jpx3.intave.block.variant.convert.ConversionBridge;
import de.jpx3.intave.klass.rewrite.PatchyLoadingInjector;
import org.bukkit.Material;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

final class BlockVariantConverter {
  private static final ConversionBridge conversionBridge;

  static {
    ClassLoader classLoader = IntavePlugin.class.getClassLoader();
    String className = "";
    if (MinecraftVersions.VER1_16_0.atOrAbove()) {
      className = "de.jpx3.intave.block.variant.convert.v16ConversionBridge";
    } else if (MinecraftVersions.VER1_14_0.atOrAbove()) {
      className = "de.jpx3.intave.block.variant.convert.v14ConversionBridge";
    } else if (MinecraftVersions.VER1_13_0.atOrAbove()) {
      className = "de.jpx3.intave.block.variant.convert.v13ConversionBridge";
    } else {
      className = "de.jpx3.intave.block.variant.convert.v8ConversionBridge";
    }
    Class<ConversionBridge> bridgeClass = PatchyLoadingInjector.loadUnloadedClassPatched(classLoader, className);
    try {
      conversionBridge = bridgeClass.newInstance();
    } catch (Exception exception) {
      throw new RuntimeException(exception);
    }
  }

  static Map<Integer, BlockVariant> translateSettings(Material type, Map<Integer, Object> indexToNative) {
    if (indexToNative.isEmpty()) {
      return Collections.emptyMap();
    }
    Map<Integer, BlockVariant> indexToVariant = new HashMap<>();
    indexToNative.forEach((key, nativeData) ->
      indexToVariant.put(key, translateSetting(type, nativeData, key))
    );
    return indexToVariant;
  }

  static final BlockVariant EMPTY = new EmptyBlockVariant();

  private static BlockVariant translateSetting(Material type, Object blockData, int variantIndex) {
    Map<Setting<?>, Comparable<?>> settings = conversionBridge.settingsOf(blockData);
    if (settings.isEmpty()) {
      return EMPTY;
    }
    return new IndexedBlockVariant(type, settings, variantIndex);
  }
}
