package de.jpx3.intave.block.fluid.old;

import de.jpx3.intave.klass.rewrite.PatchyAutoTranslation;
import de.jpx3.intave.klass.rewrite.PatchyTranslateParameters;
import de.jpx3.intave.share.NativeVector;
import de.jpx3.intave.share.link.WrapperConverter;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.meta.MovementMetadata;
import net.minecraft.server.v1_14_R1.BlockPosition;
import net.minecraft.server.v1_14_R1.IBlockAccess;
import net.minecraft.server.v1_14_R1.TagsFluid;
import net.minecraft.server.v1_14_R1.World;

@PatchyAutoTranslation
final class v14FluidResolver extends FluidResolver {
  @Override
  @PatchyAutoTranslation
  protected Fluid fluidAt(User user, int x, int y, int z) {
    MovementMetadata movementData = user.meta().movement();
    World world = (World) movementData.nmsWorld();
    IBlockAccess blockAccess = world.getChunkProvider().c(x >> 4, z >> 4);
    if (blockAccess == null) {
      return Fluid.empty();
    }
    net.minecraft.server.v1_14_R1.Fluid fluid = blockAccess.getFluid(new BlockPosition(x, y, z));
    FluidTag fluidTag = resolveFluidTagOf(fluid);
    if (fluidTag == FluidTag.EMPTY) {
      return Fluid.empty();
    }
    float height = fluid.f();
    return Fluid.of(fluidTag, fluid.isSource(), height);
  }

  @PatchyAutoTranslation
  @PatchyTranslateParameters
  private FluidTag resolveFluidTagOf(net.minecraft.server.v1_14_R1.Fluid fluid) {
    if (fluid.isEmpty()) {
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
    World world = (World) movementData.nmsWorld();
    IBlockAccess blockAccess = world.getChunkProvider().c(x >> 4, z >> 4);
    if (blockAccess == null) {
      return NativeVector.ZERO;
    }
    BlockPosition blockPosition = new BlockPosition(x, y, z);
    return WrapperConverter.vectorFromVec3D(
      blockAccess.getFluid(blockPosition).c(blockAccess, blockPosition)
    );
  }
}