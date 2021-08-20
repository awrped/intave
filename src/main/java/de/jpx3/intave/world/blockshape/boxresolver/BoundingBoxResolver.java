package de.jpx3.intave.world.blockshape.boxresolver;

import de.jpx3.intave.access.IntaveInternalException;
import de.jpx3.intave.reflect.patchy.PatchyClassSwitchLoader;

import static de.jpx3.intave.adapter.MinecraftVersions.*;

public final class BoundingBoxResolver {
  private static ResolverPipeline resolver;

  public static void setup() {
    PatchyClassSwitchLoader<?> acbbResolver = PatchyClassSwitchLoader
      .builderFor("de.jpx3.intave.world.blockshape.boxresolver.drill.acbbs.v{ver}AlwaysCollidingBoundingBox")
      .withVersions(VER1_8_0, VER1_9_0, VER1_12_0)
      .ignoreFrom(VER1_13_0)
      .complete();

    acbbResolver.loadIfAvailable();

    PatchyClassSwitchLoader<ResolverPipeline> drillResolver = PatchyClassSwitchLoader
      .<ResolverPipeline>builderFor("de.jpx3.intave.world.blockshape.boxresolver.drill.v{ver}BoundingBoxDrill")
      .withVersions(VER1_8_0, VER1_9_0, VER1_12_0, VER1_13_0, VER1_14_0, VER1_17_1)
      .complete();

    // server resolver
    resolver = drillResolver.newInstance();
    if (VER1_14_0.atOrAbove()) {
      // cache
      resolver = new VariantCachePipe(resolver);
    }
    // corrupted filter
    resolver = new CorruptedFilteringPipe(resolver);
    // empty prefilter
    resolver = new EmptyPrefetchPipe(resolver);
    // cube prefilter
    resolver = new CubeMemoryPipe(resolver);
    // patch reshaper
    resolver = new PatcherReshaperPipe(resolver);
  }

  public static ResolverPipeline pipelineHead() {
    return resolver;
  }

  private static <T> T instanceOf(String className) {
    try {
      //noinspection unchecked
      return (T) Class.forName(className).newInstance();
    } catch (InstantiationException | IllegalAccessException | ClassNotFoundException exception) {
      throw new IntaveInternalException(exception);
    }
  }
}
