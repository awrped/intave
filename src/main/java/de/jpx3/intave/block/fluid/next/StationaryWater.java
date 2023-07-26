package de.jpx3.intave.block.fluid.next;

final class StationaryWater implements Liquid {
  private static final StationaryWater INSTANCE = new StationaryWater();

  @Override
  public boolean isOfWater() {
    return true;
  }

  @Override
  public boolean isOfLava() {
    return false;
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
    return "StationaryWater{}";
  }

  public static StationaryWater of() {
    return INSTANCE;
  }
}
