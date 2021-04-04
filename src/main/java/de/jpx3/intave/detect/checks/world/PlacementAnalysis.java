package de.jpx3.intave.detect.checks.world;

import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.detect.IntaveCheck;
import de.jpx3.intave.detect.checks.world.placementanalysis.PlacementInvalidFacingPattern;
import org.bukkit.entity.Player;

public final class PlacementAnalysis extends IntaveCheck {
  private final IntavePlugin plugin;

  public PlacementAnalysis(IntavePlugin plugin) {
    super("PlacementAnalysis", "placementanalysis");
    this.plugin = plugin;
    this.setupSubChecks();
  }

  public void processViolation(Player player) {
    plugin.violationProcessor().processViolation(player, 1, "PlacementAnalysis", "suspicious block-placement", "");
  }

  public void setupSubChecks() {
    appendCheckPart(new PlacementInvalidFacingPattern(this));
  }
}