package de.jpx3.intave.event.punishment;

public enum AttackCancelType {
  HEAVY("heavy"), // Damage Cancel Request Heavy
  MEDIUM("medium"), // Damage Cancel Request Medium
  LIGHT("light"), // Damage Cancel Request Light
  BLOCKING("blocking"); // Damage Cancel Request Blocking

  private final String typeName;

  AttackCancelType(String name) {
    this.typeName = name;
  }

  public String typeName() {
    return typeName;
  }
}