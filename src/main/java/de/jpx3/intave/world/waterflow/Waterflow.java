package de.jpx3.intave.world.waterflow;

import com.comphenix.protocol.utility.MinecraftVersion;
import de.jpx3.intave.access.IntaveInternalException;
import de.jpx3.intave.adapter.ProtocolLibAdapter;
import de.jpx3.intave.tools.wrapper.WrappedAxisAlignedBB;
import de.jpx3.intave.user.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Waterflow {
  private static AbstractWaterflow engine;
  private static List<AbstractWaterflow> availableEngines = new ArrayList<>();

  public static void setup() {
    registerEngine(NetherUpdateWaterflow.class);
    registerEngine(BeeUpdateWaterflow.class);
    registerEngine(VillageUpdateWaterflow.class);
    registerEngine(AquaticUpdateWaterflow.class);
    registerEngine(UnknownWaterflow.class);

    selectAppropriateEngine();
  }

  private static void registerEngine(Class<? extends AbstractWaterflow> engineClass) {
    AbstractWaterflow engine;
    try {
      engine = engineClass.newInstance();
    } catch (InstantiationException | IllegalAccessException exception) {
      throw new IntaveInternalException(exception);
    }
    availableEngines.add(engine);
  }

  private static void selectAppropriateEngine() {
    MinecraftVersion currentVersion = ProtocolLibAdapter.serverVersion();
    engine = availableEngines.stream().filter(availableEngine -> availableEngine.appliesToAtLeast(currentVersion)).findFirst().orElse(engine);
    try {
      engine.setup();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
    availableEngines = Collections.emptyList();
  }

  public static boolean areEyesInFluid(User user, double positionX, double positionY, double positionZ) {
    return engine.areEyesInFluid(user, positionX, positionY, positionZ);
  }

  public static boolean handleFluidAcceleration(User user, WrappedAxisAlignedBB boundingBox) {
    return engine.handleFluidAcceleration(user, boundingBox);
  }

  public static boolean fluidStateEmpty(User user, double x, double y, double z) {
    return engine.fluidStateEmpty(user, x, y, z);
  }
}
