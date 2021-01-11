package de.jpx3.intave.world.block;

import com.comphenix.protocol.wrappers.BlockPosition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public final class LegacyBlockDamageResolver implements BlockDamageResolver{
  @Override
  public float blockDamage(Player player, ItemStack itemInHand, BlockPosition blockPosition) {
    return 0;
  }
}
