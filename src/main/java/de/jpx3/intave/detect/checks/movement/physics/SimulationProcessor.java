package de.jpx3.intave.detect.checks.movement.physics;

import de.jpx3.intave.user.User;
import de.jpx3.intave.world.collider.complex.ComplexColliderSimulationResult;

public interface SimulationProcessor {
  ComplexColliderSimulationResult simulate(User user, Simulator simulator);

  default ComplexColliderSimulationResult simulateMovementWithoutKeyPress(
    User user, Simulator simulator
  ) {
    return simulateMovementWithKeyPress(user, simulator,0, 0, false);
  }

  ComplexColliderSimulationResult simulateMovementWithKeyPress(User user, Simulator simulator, int forward, int strafe, boolean jumped);
}
