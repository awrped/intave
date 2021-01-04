package de.jpx3.intave.tools.sync;

import de.jpx3.intave.IntavePlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

public final class Synchronizer {
  private final static BukkitScheduler scheduler = Bukkit.getScheduler();

  public static void synchronize(Runnable runnable) {
    scheduler.runTask(IntavePlugin.singletonInstance(), runnable);
  }

  public static void synchronizeDelayed(Runnable runnable, int ticks) {
    scheduler.runTaskLater(IntavePlugin.singletonInstance(), runnable, ticks);
  }
}