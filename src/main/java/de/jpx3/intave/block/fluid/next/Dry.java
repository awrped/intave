package de.jpx3.intave.block.fluid.next;

public class Dry implements Liquid {
  private static final Dry INSTANCE = new Dry();

  @Override
  public boolean isOfWater() {
    return false;
  }

  @Override
  public boolean isOfLava() {
    return false;
  }

  @Override
  public boolean isDry() {
    return true;
  }

  @Override
  public float height() {
    return 0;
  }

  @Override
  public boolean source() {
    return false;
  }

  public static Dry of() {
    return INSTANCE;
  }
}
