package de.jpx3.intave.detect.checks.other;

import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.detect.CheckViolationLevelDecrementer;
import de.jpx3.intave.detect.IntaveCheck;
import de.jpx3.intave.detect.checks.other.inventoryclickanalysis.InventoryClickDelayAnalyzer;
import de.jpx3.intave.detect.checks.other.inventoryclickanalysis.InventoryClickNotOpenCheck;
import de.jpx3.intave.detect.checks.other.inventoryclickanalysis.InventoryClickOnMoveCheck;

public final class InventoryClickAnalysis extends IntaveCheck {
  public final static double MAX_VL_DECREMENT_PER_SECOND = 1;
  private final CheckViolationLevelDecrementer decrementer;

  public InventoryClickAnalysis(IntavePlugin plugin) {
    super("InventoryClickAnalysis", "inventoryclickanalysis");

    decrementer = new CheckViolationLevelDecrementer(this, MAX_VL_DECREMENT_PER_SECOND);

    this.setupChecks();
  }

  private void setupChecks() {
    appendCheckPart(new InventoryClickOnMoveCheck(this));
    appendCheckPart(new InventoryClickNotOpenCheck(this));
    appendCheckPart(new InventoryClickDelayAnalyzer(this));
  }

  public CheckViolationLevelDecrementer decrementer() {
    return decrementer;
  }
}