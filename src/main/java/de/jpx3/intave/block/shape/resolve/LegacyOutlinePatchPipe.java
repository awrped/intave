package de.jpx3.intave.block.shape.resolve;

import de.jpx3.intave.block.shape.BlockShape;
import de.jpx3.intave.block.shape.ShapeResolverPipeline;
import de.jpx3.intave.block.type.MaterialSearch;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Set;

public class LegacyOutlinePatchPipe implements ShapeResolverPipeline {
  private final ShapeResolverPipeline forward;

  public LegacyOutlinePatchPipe(ShapeResolverPipeline forward) {
    this.forward = forward;
  }

  @Override
  public BlockShape collisionShapeOf(World world, Player player, Material type, int variantIndex, int posX, int posY, int posZ) {
    return forward.collisionShapeOf(world, player, type, variantIndex, posX, posY, posZ);
  }

  @Override
  public BlockShape outlineShapeOf(World world, Player player, Material type, int variantIndex, int posX, int posY, int posZ) {
    if (affected(type)) {
      // use collision shape for outline
      return forward.collisionShapeOf(world, player, type, variantIndex, posX, posY, posZ);
    } else {
      return forward.outlineShapeOf(world, player, type, variantIndex, posX, posY, posZ);
    }
  }

  private final Set<Material> affectedMaterials = MaterialSearch.materialsThatContain(
    "LADDER",
    "STAIRS"
  );

  private boolean affected(Material type) {
    return affectedMaterials.contains(type);
  }

  @Override
  public void downstreamTypeReset(Material type) {
    forward.downstreamTypeReset(type);
  }
}
