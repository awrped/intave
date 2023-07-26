package de.jpx3.intave.block.fluid.old;

import static de.jpx3.intave.block.fluid.old.FluidTag.*;

public final class Fluid {
  private static final Fluid EMPTY_FLUID = new Fluid(FluidTag.EMPTY, true, 0);

  private final FluidTag fluidTag;
  private final boolean empty;
  private final boolean source;
  private final float height;

  private Fluid(FluidTag fluidTag, boolean source, float height) {
    this.fluidTag = fluidTag;
    this.source = source;
    this.height = height;
    this.empty = fluidTag == EMPTY;
  }

  public boolean isOfWater() {
    return isOf(WATER);
  }

  public boolean isOfLava() {
    return isOf(LAVA);
  }

  private boolean isOf(FluidTag fluidTag) {
    return !empty && this.fluidTag == fluidTag;
  }

  public boolean isEmpty() {
    return empty;
  }

  public float height() {
    return height;
  }

  public boolean source() {
    return source;
  }

  public static Fluid empty() {
    return EMPTY_FLUID;
  }

  // 2^5 states + one empty state
  private static final Fluid[] FLUID_UNIVERSE = new Fluid[(1 << 5) + 1];

  public static Fluid of(FluidTag fluidTag, boolean source, float height) {
    if (fluidTag == FluidTag.EMPTY) {
      return EMPTY_FLUID;
    }
    int index = cacheIndex(fluidTag, source, height);
    Fluid fluid = FLUID_UNIVERSE[index];
    if (fluid == null) {
      fluid = new Fluid(fluidTag, source, height);
      FLUID_UNIVERSE[index] = fluid;
    }
    return fluid;
  }

  private static int cacheIndex(FluidTag fluidTag, boolean source, float height) {
    if (fluidTag == FluidTag.EMPTY || height == 0) {
      return 1 << 5;
    }
    int index = 0;
    index |= (fluidTag == WATER ? 0 : 1);
    index |= (source || height == 1 ? 0 : (int) (height * 9)) << 1;
    return index;
  }

  @Override
  public String toString() {
    switch (fluidTag) {
      case EMPTY:
        return "EMPTY";
      case WATER:
        if (source) {
          return "WATER_SOURCE";
        }
        return "WATER_" + height;
      case LAVA:
        if (source) {
          return "LAVA_SOURCE";
        }
        return "LAVA_" + height;
    }
    return "UNKNOWN";
  }
}