package de.jpx3.intave.module.violation;

import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.check.Check;
import de.jpx3.intave.module.Module;
import de.jpx3.intave.module.linker.bukkit.BukkitEventSubscription;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserRepository;
import de.jpx3.intave.user.storage.ShortTermViolationStorage;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShortTermViolationRecovery extends Module {

  @BukkitEventSubscription
  public void onPlayerJoin(PlayerJoinEvent event) {
    if (!shouldRecoverVL()) {
      return;
    }
    User user = UserRepository.userOf(event.getPlayer());
    user.onStorageReady(storage -> {
      ShortTermViolationStorage stvs = user.storageOf(ShortTermViolationStorage.class);
      if (stvs == null || System.currentTimeMillis() - stvs.issuedAt() > 1000 * 60 * 5) {
        return;
      }
      Map<String, Map<String, Double>> violationLevels = user.meta().violationLevel().violationLevel;
      for (Check check : plugin.checks().checks()) {
        String checkName = check.name().toLowerCase();
        Map<String, Double> thresholdsMap = stvs.violationsFor(checkName);
        for (Map.Entry<String, Double> entry : thresholdsMap.entrySet()) {
          boolean leftToExecute = hasCommandsLeftToExecute(checkName, entry.getKey(), entry.getValue().intValue());
          if (leftToExecute) {
            violationLevels.computeIfAbsent(checkName, k -> new HashMap<>())
              .compute(entry.getKey(), (k, v) -> v == null ? entry.getValue() : Math.max(v, entry.getValue()));
          }
        }
      }
    });
  }

  @BukkitEventSubscription
  public void onPlayerQuit(PlayerQuitEvent event) {
    if (!shouldRecoverVL()) {
      return;
    }
    User user = UserRepository.userOf(event.getPlayer());
    user.onStorageReady(storage -> {
      ShortTermViolationStorage stvs = user.storageOf(ShortTermViolationStorage.class);
      if (stvs == null) {
        return;
      }
      Map<String, Map<String, Double>> violationLevels = user.meta().violationLevel().violationLevel;
      violationLevels.forEach((checkName, thresholdsMap) -> {
        thresholdsMap.forEach((threshold, value) -> {
          if (value > 5) {
            stvs.setViolation(checkName, threshold, value);
          }
        });
      });
    });
  }


  private boolean shouldRecoverVL() {
    return IntavePlugin.singletonInstance().settings().getBoolean("storage.recover-vl", true);
  }

  private boolean hasCommandsLeftToExecute(String checkName, String thresholds, int currentVL) {
    Map<Integer, List<String>> commandList =
      IntavePlugin.singletonInstance().checks().searchCheck(checkName).configuration().settings().thresholdsBy(thresholds);
    if (commandList == null) {
      return false;
    }
    return commandList.entrySet().stream().anyMatch(entry -> currentVL < entry.getKey() - 10);
  }
}
