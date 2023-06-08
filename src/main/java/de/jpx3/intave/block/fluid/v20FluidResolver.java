package de.jpx3.intave.block.fluid;

import de.jpx3.intave.klass.Lookup;
import de.jpx3.intave.klass.locate.MethodSearchBySignature;
import de.jpx3.intave.klass.rewrite.PatchyAutoTranslation;
import de.jpx3.intave.klass.rewrite.PatchyTranslateParameters;
import de.jpx3.intave.library.asm.Type;
import de.jpx3.intave.share.NativeVector;
import de.jpx3.intave.share.link.WrapperConverter;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.meta.MovementMetadata;
import net.minecraft.core.BlockPosition;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.World;
import net.minecraft.world.level.chunk.IChunkProvider;
import net.minecraft.world.level.chunk.LightChunk;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@PatchyAutoTranslation
final class v20FluidResolver extends FluidResolver {
  private static Object TAG_KEY_WATER = null;
  private static Object TAG_KEY_LAVA = null;

  private static final MethodHandle resolveTagKey;

  static {
    try {
      TAG_KEY_WATER = Lookup.serverField("TagsFluid", "WATER").get(null);
      TAG_KEY_LAVA = Lookup.serverField("TagsFluid", "LAVA").get(null);
    } catch (IllegalAccessException exception) {
      exception.printStackTrace();
    }
    resolveTagKey = MethodSearchBySignature
      .ofClass(Lookup.serverClass("Fluid"))
      .withParameters(new Class[]{Lookup.serverClass("TagKey")})
      .withReturnType(Boolean.TYPE).search().findFirstOrThrow();
  }

  @Override
  @PatchyAutoTranslation
  protected Fluid fluidAt(User user, int x, int y, int z) {
    MovementMetadata movementData = user.meta().movement();
    World world = (World) movementData.nmsWorld();
    IBlockAccess blockAccess = findChunk(world.getChunkProvider(), x >> 4, z >> 4);
    if (blockAccess == null) {
      return Fluid.empty();
    }
    net.minecraft.world.level.material.Fluid fluid = blockAccess.getFluid(new BlockPosition(x, y, z));
    FluidTag fluidTag = resolveFluidTagOf(fluid);
    if (fluidTag == FluidTag.EMPTY) {
      return Fluid.empty();
    }
    float height = fluid.d();
    return Fluid.of(fluidTag, fluid.isSource(), height);
  }

  @PatchyAutoTranslation
  @PatchyTranslateParameters
  private FluidTag resolveFluidTagOf(net.minecraft.world.level.material.Fluid fluid) {
    return fluid.isEmpty() ? FluidTag.EMPTY : tagKeyResolve(fluid);
  }

  @PatchyAutoTranslation
  @PatchyTranslateParameters
  private FluidTag tagKeyResolve(Object fluid) {
    try {
      boolean water = (boolean) resolveTagKey.invoke(fluid, TAG_KEY_WATER);
      boolean lava = !water && (boolean) resolveTagKey.invoke(fluid, TAG_KEY_LAVA);
      return FluidTag.select(water, lava);
    } catch (Throwable exception) {
      exception.printStackTrace();
    }
    return FluidTag.EMPTY;
  }

  @Override
  @PatchyAutoTranslation
  protected NativeVector flowVectorAt(User user, int x, int y, int z) {
    MovementMetadata movementData = user.meta().movement();
    World world = (World) movementData.nmsWorld();
    IBlockAccess blockAccess = findChunk(world.getChunkProvider(), x >> 4, z >> 4);
    if (blockAccess == null) {
      return NativeVector.ZERO;
    }
    BlockPosition blockPosition = new BlockPosition(x, y, z);
    return WrapperConverter.vectorFromVec3D(blockAccess.getFluid(blockPosition).c(blockAccess, blockPosition));
  }

  private LightChunk findChunk(IChunkProvider server, int x, int z) {
    Class<?> chunk = Lookup.serverClass("LightChunk");
    Method providerMethod = Lookup.serverMethod("ChunkProviderServer", "c", new Type[]{Type.INT_TYPE, Type.INT_TYPE}, Type.getType(chunk));
    try {
      return (LightChunk) providerMethod.invoke(server, x, z);
    } catch (IllegalAccessException | InvocationTargetException ignored) {
    }
    return null;
  }
}
