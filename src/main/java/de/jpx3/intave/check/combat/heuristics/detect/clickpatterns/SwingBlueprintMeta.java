package de.jpx3.intave.check.combat.heuristics.detect.clickpatterns;

import de.jpx3.intave.user.meta.CheckCustomMetadata;

import java.util.ArrayList;
import java.util.List;

public abstract class SwingBlueprintMeta extends CheckCustomMetadata {
  protected final List<Integer> delays = new ArrayList<>();
  protected final List<Integer> delaysDelta = new ArrayList<>();
  protected int delay, lastDelay;
  protected int doubleClicks;
  protected int lastAttack; // In client ticks
  protected boolean placedBlock;
}
