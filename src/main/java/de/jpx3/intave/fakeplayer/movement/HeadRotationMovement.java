package de.jpx3.intave.fakeplayer.movement;

import java.util.concurrent.ThreadLocalRandom;

public class HeadRotationMovement {
  private final static double MAX_PITCH_ROTATION = 30;
  private final float sensitivity = CameraUtils.generateRandomSensitivity();
  public float prevRotationYaw = 0.0f, prevRotationPitch = 0.0f;
  public float rotationYaw = 0.0f, rotationPitch = 0.0f;

  public void updateHeadRotation(
    double motionX, double motionZ, double distanceMoved
  ) {
    this.prevRotationYaw = this.rotationYaw;
    this.prevRotationPitch = this.rotationPitch;
    float updatedPitch = updatePitch();
    float updatedYaw = distanceMoved != 0 ? motionYaw(motionX, motionZ) : this.rotationYaw;
    updatedYaw %= 360;
    this.rotationPitch = applySensitivity(updatedPitch);
    this.rotationYaw = updatedYaw;
  }

  private float updatePitch() {
    float newPitch = (float) ThreadLocalRandom.current().nextDouble(prevRotationPitch - 15.0, prevRotationPitch + 15.0);
    if (inRange(newPitch)) {
      prevRotationPitch = applySensitivity(newPitch);
    }
    return newPitch;
  }

  private float motionYaw(double motionX, double motionZ) {
    return (float) (Math.atan2(motionZ, motionX) * 180.0 / Math.PI) - 90.0f;
  }

  private boolean inRange(double value) {
    return value >= -30.0 && value <= HeadRotationMovement.MAX_PITCH_ROTATION;
  }

  private float applySensitivity(float f) {
    return f - (f % sensitivity);
  }
}