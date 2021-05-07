package de.jpx3.intave.world.collision.resolver.pipeline;

import de.jpx3.intave.diagnostics.BoundingBoxAccessFlowStudy;
import de.jpx3.intave.tools.client.MaterialLogic;
import de.jpx3.intave.tools.wrapper.WrappedAxisAlignedBB;
import de.jpx3.intave.world.collision.resolver.BoundingBoxResolvePipelineElement;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public final class DynamicEmptyBlockPreFilter implements BoundingBoxResolvePipelineElement {
  private final BoundingBoxResolvePipelineElement forward;

  public DynamicEmptyBlockPreFilter(BoundingBoxResolvePipelineElement forward) {
    this.forward = forward;
  }

  @Override
  public List<WrappedAxisAlignedBB> nativeResolve(World world, Player player, Material type, int blockState, int posX, int posY, int posZ) {
    if(isEmpty(type)) {
      BoundingBoxAccessFlowStudy.increaseDynamic();
      return Collections.emptyList();
    }
    return forward.nativeResolve(world, player, type, blockState, posX, posY, posZ);
  }

  @Override
  public List<WrappedAxisAlignedBB> customResolve(World world, Player player, Material type, int blockState, int posX, int posY, int posZ) {
    if(isEmpty(type)) {
      BoundingBoxAccessFlowStudy.increaseDynamic();
      return Collections.emptyList();
    }
    return forward.customResolve(world, player, type, blockState, posX, posY, posZ);
  }

  private boolean isEmpty(Material type) {
    if(MaterialLogic.isLiquid(type)) {
      return true;
    }
    switch (type) {
      case AIR:
        return true;
    }

    return false;
  }
}
