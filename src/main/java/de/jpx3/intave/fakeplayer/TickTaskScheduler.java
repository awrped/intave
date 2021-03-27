package de.jpx3.intave.fakeplayer;

import org.bukkit.Bukkit;

public interface TickTaskScheduler {
  void startTickScheduler();

  int taskId();

  default void stopTickScheduler() {
    int taskId = taskId();
    Bukkit.getScheduler().cancelTask(taskId);
  }
}
