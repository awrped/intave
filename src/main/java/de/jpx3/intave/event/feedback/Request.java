package de.jpx3.intave.event.feedback;

import org.bukkit.entity.Player;

public final class Request<T> {
  private final Callback<T> callback;
  private final T obj;
  private final short key;
  private final long time;
  private final long num;

  Request(Callback<T> Callback, T obj, short key, long num) {
    this.callback = Callback;
    this.obj = obj;
    this.key = key;
    this.num = num;
    this.time = System.currentTimeMillis();
  }

  public void acknowledge(Player player) {
    callback.success(player, obj);
  }

  public long passedTime() {
    return System.currentTimeMillis() - this.time;
  }

  public short key() {
    return key;
  }

  public long num() {
    return num;
  }

  public long requested() {
    return time;
  }
}