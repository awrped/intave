package de.jpx3.intave.command.stages;

import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.command.CommandStage;
import de.jpx3.intave.command.SubCommand;
import de.jpx3.intave.detect.CheckStatistics;
import de.jpx3.intave.detect.IntaveCheck;
import de.jpx3.intave.diagnostics.timings.Timing;
import de.jpx3.intave.diagnostics.timings.Timings;
import de.jpx3.intave.tools.MathHelper;
import de.jpx3.intave.tools.annotate.Native;
import de.jpx3.intave.user.User;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public final class IntaveRootStage extends CommandStage {
  private static IntaveRootStage singletonInstance;
  private final IntavePlugin plugin;

  private IntaveRootStage() {
    super(IntaveCommandStage.singletonInstance(), "root", 1);
    plugin = IntavePlugin.singletonInstance();
  }

  @SubCommand(
    selectors = "timings",
    usage = "",
    description = "Output timing data",
    permission = "sibyl"
  )
  @Native
  public void timingsCommand(User user) {
    Player player = user.player();
    if(plugin.sibylIntegrationService().authentication().isAuthenticated(player)) {
      player.sendMessage(ChatColor.RED + "Loading timings...");
      List<Timing> timings = new ArrayList<>(Timings.timingPool());
      timings.sort(Timing::compareTo);

      timings.forEach(timing -> {
        boolean suspicious = timing.getAverageCallDurationInMillis() > 0.5d;
        boolean dumping = timing.getAverageCallDurationInMillis() > 1.5d;
        String message = String.format("%s: %s::%sms (%s&f ms/c)",
          timing.getTimingName(),
          timing.getRecordedCalls(),
          MathHelper.formatDouble(timing.totalDurationMillis(), 4),
          (suspicious ? (dumping ? ChatColor.RED : ChatColor.YELLOW ) : ChatColor.GREEN) + "" +
            MathHelper.formatDouble(timing.getAverageCallDurationInMillis(), 8)
        );
        player.sendMessage(ChatColor.translateAlternateColorCodes('&',message));
      });
    }
  }

  @SubCommand(
    selectors = "statistics",
    usage = "",
    description = "Output check statistics",
    permission = "sibyl"
  )
  @Native
  public void checkStatisticsCommand(User user) {
    Player player = user.player();
    player.sendMessage(ChatColor.RED + "Loading statistics...");
    for (IntaveCheck check : plugin.checkService().checks()) {
      CheckStatistics statistics = check.statistics();
      double processed = statistics.totalProcessed();
      double violations = statistics.totalViolations();
      double failed = statistics.totalFails();
      long passed = statistics.totalPasses();

      if (processed == 0) {
        continue;
      }

      String failedRate = MathHelper.formatDouble(failed / processed * 100, 5);
      String violatedRate = MathHelper.formatDouble(violations / processed * 100, 5);

      String message = String.format("Check/%s: %s::%s%% / vio %s%%", check.name(), passed, failedRate, violatedRate);
      player.sendMessage(ChatColor.WHITE + message);
    }
  }

  public static IntaveRootStage singletonInstance() {
    if(singletonInstance == null) {
      singletonInstance = new IntaveRootStage();
    }
    return singletonInstance;
  }
}
