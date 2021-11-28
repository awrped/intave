package de.jpx3.intave.check.movement;

import com.comphenix.protocol.events.PacketEvent;
import de.jpx3.intave.IntaveLogger;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.check.Check;
import de.jpx3.intave.check.CheckViolationLevelDecrementer;
import de.jpx3.intave.check.movement.timer.Balance;
import de.jpx3.intave.check.movement.timer.MovementFrequency;

public final class Timer extends Check {
  private final CheckViolationLevelDecrementer decrementer;

  private final boolean highToleranceMode;
  private final Balance balance = new Balance(this);

  public Timer(IntavePlugin plugin) {
    super("Timer", "timer");
    this.decrementer = new CheckViolationLevelDecrementer(this, 0.2);

    highToleranceMode = configuration().settings().boolBy("high-tolerance", false);
    if (highToleranceMode) {
      IntaveLogger.logger().info("Enabled high ping tolerance");
    }

    appendCheckPart(balance);
  //  appendCheckPart(new MovementFrequency(this));
  }

  public void receiveMovement(PacketEvent event) {
    balance.receiveMovement(event);
  }

  @Override
  public boolean enabled() {
    return true;
  }

  @Override
  public boolean performLinkage() {
    return true;
  }

  public boolean highToleranceMode() {
    return highToleranceMode;
  }

  public CheckViolationLevelDecrementer decrementer() {
    return decrementer;
  }
}
