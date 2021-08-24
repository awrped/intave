package de.jpx3.intave.executor;

import de.jpx3.intave.cleanup.Shutdown;
import io.netty.util.internal.ConcurrentSet;
import org.bukkit.Bukkit;

import java.util.Collections;
import java.util.Set;

public final class TaskTracker {
  private final static Set<Integer> runningTasks = new ConcurrentSet<>();

  public static void setup() {
    Shutdown.addTask(TaskTracker::stopAll);
  }

  public static void begun(int taskId) {
    runningTasks.add(taskId);
  }

  public static void stopped(int taskId) {
    runningTasks.remove(taskId);
  }

  public static void stopAll() {
    for (Integer runningTask : Collections.unmodifiableSet(runningTasks)) {
      Bukkit.getScheduler().cancelTask(runningTask);
      stopped(runningTask);
    }
  }
}
