package de.jpx3.intave.world.block;

import com.comphenix.protocol.wrappers.BlockPosition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Class generated using IntelliJ IDEA
 * Created by Richard Strunk 2021
 */

public interface BlockDamageResolver {
  float blockDamage(Player player, ItemStack itemInHand, BlockPosition blockPosition);
}
