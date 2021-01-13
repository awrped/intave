package de.jpx3.intave.access;

import com.google.common.base.Preconditions;
import de.jpx3.intave.IntavePlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Created by Jpx3 on 10.11.2017.
 */

public final class AsyncIntaveCommandTriggerEvent extends AbstractIntaveExternalEvent implements Cancellable {
  private Player punished;
  private String command;
  private boolean delayedExecution;
  private boolean cancelled;

  private AsyncIntaveCommandTriggerEvent(Player punished, String command, boolean delayedExecution) {
    this.punished = punished;
    this.command = command;
    this.delayedExecution = delayedExecution;
    this.setCancelled(false);
  }

  public Player player() {
    return punished;
  }

  public String command() {
    return command;
  }

  public void setCommand(String command) {
    Preconditions.checkNotNull(command);

    this.command = command;
  }

  public boolean delayedExecute() {
    return delayedExecution;
  }

  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  @Override
  public void setCancelled(boolean cancelled) {
    this.cancelled = cancelled;
  }

  @Override
  public void clearPlayerReference() {
    punished = null;
  }

  public void renew(Player punished, String command, boolean delayedExecute) {
    this.punished = punished;
    this.command = command;
    this.delayedExecution = delayedExecute;
    this.setCancelled(false);
  }

  public static AsyncIntaveCommandTriggerEvent empty(IntavePlugin handle) {
    return construct(handle, null, "empty", false);
  }

  public static AsyncIntaveCommandTriggerEvent construct(IntavePlugin plugin, Player punished, String command, boolean isWaveExecuted) {
    if(plugin != IntavePlugin.singletonInstance()) {
      throw new IllegalStateException();
    }
    return new AsyncIntaveCommandTriggerEvent(punished, command, isWaveExecuted);
  }
}
