package de.jpx3.intave.world.collision.resolver.pipeline;

import de.jpx3.intave.tools.wrapper.WrappedAxisAlignedBB;
import de.jpx3.intave.world.collision.resolver.BoundingBoxResolvePipelineElement;
import de.jpx3.intave.world.collision.resolver.pipeline.patcher.BoundingBoxPatcher;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.List;

public final class DynamicPatcherReshaper implements BoundingBoxResolvePipelineElement {
  private final BoundingBoxResolvePipelineElement forward;

  public DynamicPatcherReshaper(BoundingBoxResolvePipelineElement forward) {
    this.forward = forward;
  }

  @Override
  @Deprecated
  public List<WrappedAxisAlignedBB> nativeResolve(World world, Player player, Material type, int blockState, int posX, int posY, int posZ) {
    List<WrappedAxisAlignedBB> original = forward.nativeResolve(world, player, type, blockState, posX, posY, posZ);
    return player == null ? original : BoundingBoxPatcher.patch(world, player, posX, posY, posZ, type, blockState, original);
  }

  @Override
  public List<WrappedAxisAlignedBB> customResolve(World world, Player player, Material type, int blockState, int posX, int posY, int posZ) {
    List<WrappedAxisAlignedBB> original = forward.customResolve(world, player, type, blockState, posX, posY, posZ);
    return player == null ? original : BoundingBoxPatcher.patch(world, player, posX, posY, posZ, type, blockState, original);
  }
}
