package de.jpx3.intave.shade;

import java.util.ArrayList;
import java.util.List;

@Deprecated
public final class MovementKeyInput {
  private static final MovementKeyInput[][] UNIVERSE = new MovementKeyInput[3][3];
  private static final List<MovementKeyInput> VALUES_USAGE_SORTED = new ArrayList<>();
  public static final MovementKeyInput INVALID = new MovementKeyInput(-2, -2);

  public static final MovementKeyInput W_PRESS = fromKeys(1, 0);
  public static final MovementKeyInput WA_PRESS = fromKeys(1, 1);
  public static final MovementKeyInput WD_PRESS = fromKeys(1, -1);
  public static final MovementKeyInput S_PRESS = fromKeys(-1, 0);
  public static final MovementKeyInput SA_PRESS = fromKeys(-1, 1);
  public static final MovementKeyInput SD_PRESS = fromKeys(-1, -1);
  public static final MovementKeyInput A_PRESS = fromKeys(0, 1);
  public static final MovementKeyInput D_PRESS = fromKeys(0, -1);
  public static final MovementKeyInput N_PRESS = fromKeys(0, 0);

  static {
    for (int i = -1; i <= 1; i++) {
      MovementKeyInput[] strafeInputs = new MovementKeyInput[3];
      for (int j = -1; j <= 1; j++) {
        strafeInputs[j + 1] = new MovementKeyInput(i, j);
      }
      UNIVERSE[i + 1] = strafeInputs;
    }
    int[][] usageOrderedKeySets = {{1, 0}, {1, -1}, {1, 1}, {0, 0}, {0, -1}, {0, 1}, {-1, -1}, {-1, 0}, {-1, 1}};
    for (int i = 0; i < 9; i++) {
      int[] keySet = usageOrderedKeySets[i];
      VALUES_USAGE_SORTED.add(MovementKeyInput.fromKeys(keySet[0], keySet[1]));
    }
  }

  private final int forward, strafe;

  private MovementKeyInput(int forward, int strafe) {
    this.forward = forward;
    this.strafe = strafe;
  }

  public int forward() {
    return forward;
  }

  public int strafe() {
    return strafe;
  }

  public float moveForward() {
    return forward * 0.98f;
  }

  public float moveStrafe() {
    return strafe * 0.98f;
  }

  public MovementKeyInput clear() {
    return fromKeys(0, 0);
  }

  public static List<MovementKeyInput> valuesUsageSorted() {
    return VALUES_USAGE_SORTED;
  }

  public static MovementKeyInput fromKeys(int forward, int strafe) {
    if (Math.abs(forward) > 1 || Math.abs(strafe) > 1) {
      return INVALID;
    }
    return UNIVERSE[forward + 1][strafe + 1];
  }
}