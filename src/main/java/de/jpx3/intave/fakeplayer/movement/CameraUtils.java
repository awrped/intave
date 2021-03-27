package de.jpx3.intave.fakeplayer.movement;

import org.bukkit.util.Vector;

import java.util.concurrent.ThreadLocalRandom;

public final class CameraUtils {
  private final static double MIN_SENSITIVITY = 1.0;
  private final static double MAX_SENSITIVITY = 200.0;
  private final static float MAX_CAMERA_PITCH = 90.0f;

  public static float generateRandomSensitivity() {
    return (float) ThreadLocalRandom.current().nextDouble(MIN_SENSITIVITY, MAX_SENSITIVITY);
  }

  public static float limitAngleReach(float pitch) {
    if (pitch < -MAX_CAMERA_PITCH) {
      pitch = -MAX_CAMERA_PITCH;
    } else if (pitch > MAX_CAMERA_PITCH) {
      pitch = MAX_CAMERA_PITCH;
    }
    return pitch;
  }

  public static Vector getDirection(final float yaw, final float pitch) {
    Vector vector = new Vector();
    vector.setY(-Math.sin(Math.toRadians(pitch)));
    double xz = Math.cos(Math.toRadians(pitch));
    vector.setX(-xz * Math.sin(Math.toRadians(yaw)));
    vector.setZ(xz * Math.cos(Math.toRadians(yaw)));
    return vector;
  }
}