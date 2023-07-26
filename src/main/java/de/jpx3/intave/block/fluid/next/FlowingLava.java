package de.jpx3.intave.block.fluid.next;

class FlowingLava implements Liquid {
  private final float height;

  private FlowingLava(float height) {
    this.height = height;
  }

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
    return height;
  }

  @Override
  public boolean source() {
    return false;
  }

  @Override
  public String toString() {
    return "FlowingLava{" +
      "height=" + height +
      '}';
  }

  public static FlowingLava ofHeight(float height) {
    return new FlowingLava(height);
  }
}
