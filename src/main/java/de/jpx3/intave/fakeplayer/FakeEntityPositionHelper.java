package de.jpx3.intave.fakeplayer;

import de.jpx3.intave.tools.wrapper.WrappedMathHelper;

public final class FakeEntityPositionHelper {
  private final static double POSITION_CONVERT_FACTOR = 32.0D;
  private static final float FIX_CONVERT_FACTOR = 256.0F / 360.0F;

  public static int getFixCoordinate(double coordinate) {
    return (int) Math.floor(coordinate * POSITION_CONVERT_FACTOR);
  }

  public static byte getFixRotation(final float f) {
    return (byte) (f * FIX_CONVERT_FACTOR);
  }

  public static byte relativeMoveDiff(double coordinateTo, double coordinateFrom) {
    double fixedTo = WrappedMathHelper.floor(coordinateTo * POSITION_CONVERT_FACTOR);
    double fixedFrom = WrappedMathHelper.floor(coordinateFrom * POSITION_CONVERT_FACTOR);
    return (byte) (fixedTo - fixedFrom);
  }
}