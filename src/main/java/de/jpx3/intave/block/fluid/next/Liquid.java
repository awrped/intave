package de.jpx3.intave.block.fluid.next;

public interface Liquid {
  boolean isOfWater();

  boolean isOfLava();

  boolean isDry();

  float height();

  boolean source();
}
