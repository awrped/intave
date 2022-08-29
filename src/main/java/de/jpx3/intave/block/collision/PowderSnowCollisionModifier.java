package de.jpx3.intave.block.collision;

import de.jpx3.intave.block.shape.BlockShape;
import de.jpx3.intave.block.shape.BlockShapes;
import de.jpx3.intave.share.BoundingBox;
import de.jpx3.intave.user.User;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

final class PowderSnowCollisionModifier extends CollisionModifier {
  private static final BlockShape POWDER_SNOW_FROM_ABOVE = BlockShapes.originCube();

  @Override
  public BlockShape modify(User user, BoundingBox userBox, int posX, int posY, int posZ, BlockShape shape) {
    ItemStack boots = user.player().getInventory().getBoots();
    boolean leatherBootsEquipped = boots != null && boots.getType() == Material.LEATHER_BOOTS;
    // TODO Check for sneaking?
    if (leatherBootsEquipped && userBox.minY >= posY + 1) {
      BlockShape contextualized = POWDER_SNOW_FROM_ABOVE.contextualized(posX, posY + 1, posZ);
      for (BoundingBox boundingBox : contextualized.boundingBoxes()) {
        System.out.println("bb:" + boundingBox);
      }

      System.out.println(contextualized);
      return contextualized;
    } else {
      return BlockShapes.emptyShape();
    }
  }

  @Override
  public boolean matches(Material material) {
    return material.name().contains("POWDER_SNOW");
  }
}
