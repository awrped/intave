package de.jpx3.intave.module.player;

import de.jpx3.intave.analytics.PlaytimeRecorder;
import de.jpx3.intave.executor.TaskTracker;
import de.jpx3.intave.module.Module;
import de.jpx3.intave.module.linker.bukkit.BukkitEventSubscription;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserRepository;
import de.jpx3.intave.user.storage.PlaytimeStorage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlaytimeUpdater extends Module {
  @Override
  public void enable() {
    int taskId = Bukkit.getScheduler().scheduleAsyncRepeatingTask(plugin, () ->
      Bukkit.getOnlinePlayers().forEach(player -> {
        User user = UserRepository.userOf(player);
        PlaytimeRecorder recorder = plugin.analytics().recorderOf(PlaytimeRecorder.class);
        PlaytimeStorage playtimeStorage = user.storageOf(PlaytimeStorage.class);
        if (System.currentTimeMillis() - user.joined() < 30000) {
          return;
        }
        if (System.currentTimeMillis() - user.meta().movement().lastRotation > 1000 * 60 * 2) {
          playtimeStorage.incrementMinutesAfkBy(1);
          recorder.incrementAfkMinutesBy(1);
        } else {
          playtimeStorage.incrementMinutesPlayedBy(1);
          recorder.incrementActiveMinutesBy(1);
        }
      }
    ), 20 * 60, 20 * 60);
    TaskTracker.begun(taskId);
  }

  @BukkitEventSubscription
  public void on(PlayerQuitEvent quit) {
    Player player = quit.getPlayer();
    User user = UserRepository.userOf(player);
    PlaytimeStorage playtimeStorage =
      user.storageOf(PlaytimeStorage.class);
    playtimeStorage.incrementJoins();
  }
}
