package de.jpx3.intave.world.block;

import com.comphenix.protocol.wrappers.BlockPosition;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.reflect.ReflectionFailureException;
import de.jpx3.patchy.PatchyLoadingInjector;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class BlockDamage {
  private static final BlockDamageResolver blockDamageResolver;

  static {
    String resolverName = "de.jpx3.intave.world.block.LegacyBlockDamageResolver";

    ClassLoader classLoader = IntavePlugin.class.getClassLoader();
    PatchyLoadingInjector.loadUnloadedClassPatched(classLoader, resolverName);
    blockDamageResolver = instanceOf(resolverName);
  }

  private static <T> T instanceOf(String className) {
    try {
      //noinspection unchecked
      return (T) Class.forName(className).newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException exception) {
      throw new ReflectionFailureException(exception);
    }
  }

  public static float blockDamage(Player player, ItemStack itemInHand, BlockPosition blockPosition) {
    return blockDamageResolver.blockDamage(player, itemInHand, blockPosition);
  }
}
