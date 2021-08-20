package de.jpx3.intave.connect.shadow;

/**
 * Class generated using IntelliJ IDEA
 * Created by Richard Strunk 2021
 */

@Deprecated
public enum ShadowStatus {
  RESET_ID(0),
  ENABLE(1),
  DISABLE(2),

  ;

  private final int key;

  ShadowStatus(int key) {
    this.key = key;
  }

  public int key() {
    return key;
  }
}
