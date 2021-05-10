package de.jpx3.intave.user;

import com.google.common.collect.Maps;
import de.jpx3.intave.tools.annotate.Relocate;

import java.util.Map;

@Relocate
public final class UserMetaViolationLevelData {
  public double physicsVL;
  public double physicsVelocityVL;
  public double physicsInvalidMovementsInRow;
  public volatile boolean isInActiveTeleportBundle;

  public Map<String, Map<String, Double>> violationLevel = Maps.newConcurrentMap();
  public Map<String, Map<String, Double>> violationLevelGainedCounter = Maps.newConcurrentMap();
  public Map<String, Map<String, Long>> lastViolationLevelGainedCounterReset = Maps.newConcurrentMap();
}