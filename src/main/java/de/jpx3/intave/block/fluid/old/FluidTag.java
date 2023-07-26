package de.jpx3.intave.block.fluid.old;

import de.jpx3.intave.annotate.KeepEnumInternalNames;

@KeepEnumInternalNames
enum FluidTag {
  WATER,
  LAVA,
  EMPTY;

  public static FluidTag select(
    boolean water, boolean lava
  ) {
    if (water) return WATER;
    if (lava) return LAVA;
    return EMPTY;
  }
}