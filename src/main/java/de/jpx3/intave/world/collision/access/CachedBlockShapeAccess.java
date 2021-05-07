package de.jpx3.intave.world.collision.access;

import org.bukkit.Material;
import org.bukkit.World;

/**
 * Class generated using IntelliJ IDEA
 * Created by Richard Strunk 2021
 */

public interface CachedBlockShapeAccess extends BlockShapeAccess {
  /* optional methods for a resolver */

  void identityInvalidate();

  void invalidate();

  default void invalidate(int posX, int posY, int posZ) {
    invalidate0(posX + 1, posY, posZ);
    invalidate0(posX - 1, posY, posZ);
    invalidate0(posX, posY, posZ + 1);
    invalidate0(posX, posY, posZ - 1);
    invalidate0(posX, posY + 1, posZ);
    invalidate0(posX, posY - 1, posZ);
    invalidate0(posX, posY, posZ);
  }

  void invalidate0(int posX, int posY, int posZ);

  void override(World world, int posX, int posY, int posZ, Material type, int blockState);

  void invalidateOverridesInBounds(int chunkXMinPos, int chunkXMaxPos, int chunkZMinPos, int chunkZMaxPos);
}
