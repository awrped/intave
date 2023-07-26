package de.jpx3.intave.block.fluid.next;

class FlowingWater implements Liquid {
  private final float height;

  private FlowingWater(float height) {
    this.height = height;
  }

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
    return height;
  }

  @Override
  public boolean source() {
    return false;
  }

  @Override
  public String toString() {
    return "FlowingWater{" +
      "height=" + height +
      '}';
  }

  public static FlowingWater ofHeight(float height) {
    return new FlowingWater(height);
  }
}
