package de.jpx3.intave.check.combat.heuristics.detect.clickpatterns;

import de.jpx3.intave.check.combat.Heuristics;
import de.jpx3.intave.check.combat.heuristics.Anomaly;
import de.jpx3.intave.check.combat.heuristics.Confidence;
import de.jpx3.intave.user.User;

import java.util.List;

import static de.jpx3.intave.check.combat.heuristics.detect.clickpatterns.SwingDeviationHeuristics.SwingDeviationBlueprintMeta;

public final class SwingDeviationHeuristics extends SwingBlueprint<SwingDeviationBlueprintMeta> {
  public SwingDeviationHeuristics(Heuristics parentCheck) {
    super(parentCheck, SwingDeviationBlueprintMeta.class, 100, true, false);
  }

  @Override
  public void check(User user, List<Integer> delays) {
    SwingDeviationBlueprintMeta meta = metaOf(userOf(user.player()));
    double cps = clickPerSecond(delays);
    double deviation = standardDeviation(delays);
    if (deviation < 0.45) {
      if (++meta.vl >= 3) {
        String description = String.format("clicking too fast without double clicks %.2f", cps);
        Anomaly anomaly = Anomaly.anomalyOf("300", Confidence.NONE, Anomaly.Type.AUTOCLICKER, description);
        parentCheck().saveAnomaly(user.player(), anomaly);
      }
    } else {
      meta.vl = Math.max(0, meta.vl - 0.5);
    }
  }

  public static class SwingDeviationBlueprintMeta extends SwingBlueprintMeta {
    double vl;
    // Nothing yet!
  }
}