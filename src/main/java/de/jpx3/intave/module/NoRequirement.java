package de.jpx3.intave.module;

public final class NoRequirement implements Requirement {
  @Override
  public boolean fulfilled() {
    return true;
  }
}
