package de.jpx3.intave.block.fluid.next;

final class StationaryLava implements Liquid {
  private static final StationaryLava INSTANCE = new StationaryLava();

  @Override
  public boolean isOfWater() {
    return false;
  }

  @Override
  public boolean isOfLava() {
    return true;
  }

  @Override
  public boolean isDry() {
    return false;
  }

  @Override
  public float height() {
    return 1.0f;
  }

  @Override
  public boolean source() {
    return true;
  }

  @Override
  public String toString() {
    return "StationaryLava{}";
  }

  public static StationaryLava of() {
    return INSTANCE;
  }
}
