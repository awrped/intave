package de.jpx3.intave.world.fluid;

public final class WrappedFluid {
  private final static WrappedFluid EMPTY = WrappedFluid.construct(FluidTag.EMPTY, 0);

  private final FluidTag fluidTag;
  private final float height;

  private WrappedFluid(FluidTag fluidTag, float height) {
    this.fluidTag = fluidTag;
    this.height = height;
  }

  public FluidTag fluidTag() {
    return fluidTag;
  }

  public boolean isIn(FluidTag fluidTag) {
    return this.fluidTag == fluidTag;
  }

  public boolean isEmpty() {
    return fluidTag == FluidTag.EMPTY;
  }

  public float height() {
    return height;
  }

  public static WrappedFluid empty() {
    return EMPTY;
  }

  public static WrappedFluid construct(FluidTag fluidTag, float height) {
    return new WrappedFluid(fluidTag, height);
  }
}