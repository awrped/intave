package de.jpx3.intave.block.fluid.old;

import de.jpx3.intave.klass.rewrite.PatchyAutoTranslation;
import de.jpx3.intave.klass.rewrite.PatchyTranslateParameters;
import de.jpx3.intave.share.NativeVector;
import de.jpx3.intave.share.link.WrapperConverter;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.meta.MovementMetadata;
import net.minecraft.server.v1_13_R2.BlockPosition;
import net.minecraft.server.v1_13_R2.IWorldReader;
import net.minecraft.server.v1_13_R2.TagsFluid;
import net.minecraft.server.v1_13_R2.World;

@PatchyAutoTranslation
final class v13FluidResolver extends FluidResolver {
  @Override
  @PatchyAutoTranslation
  protected Fluid fluidAt(User user, int x, int y, int z) {
    MovementMetadata movementData = user.meta().movement();
    World world = (World) movementData.nmsWorld();
    if (!world.isChunkLoaded(x >> 4, z >> 4, false)) {
      return Fluid.empty();
    }
    net.minecraft.server.v1_13_R2.Fluid fluid = world.getFluid(new BlockPosition(x, y, z));
    FluidTag fluidTag = resolveFluidTagOf(fluid);
    if (fluidTag == FluidTag.EMPTY) {
      return Fluid.empty();
    }
    float height = fluid.getHeight();
    return Fluid.of(fluidTag, fluid.d(), height);
  }

  @PatchyAutoTranslation
  @PatchyTranslateParameters
  private FluidTag resolveFluidTagOf(net.minecraft.server.v1_13_R2.Fluid fluid) {
    if (fluid.e()) {
      return FluidTag.EMPTY;
    }
    boolean water = fluid.a(TagsFluid.WATER);
    boolean lava = !water && fluid.a(TagsFluid.LAVA);
    return FluidTag.select(water, lava);
  }

  @Override
  @PatchyAutoTranslation
  protected NativeVector flowVectorAt(User user, int x, int y, int z) {
    MovementMetadata movementData = user.meta().movement();
    IWorldReader world = (World) movementData.nmsWorld();
    BlockPosition blockPosition = new BlockPosition(x, y, z);
    return WrapperConverter.vectorFromVec3D(world.getFluid(blockPosition).a(world, blockPosition));
  }
}