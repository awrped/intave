package de.jpx3.intave.world.blockaccess;

import com.comphenix.protocol.wrappers.BlockPosition;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Class generated using IntelliJ IDEA
 * Created by Richard Strunk 2021
 */

public interface BlockAccessor {
  float blockDamage(Player player, ItemStack itemInHand, BlockPosition blockPosition);

  boolean replacementPlace(World world, BlockPosition blockPosition);
}
