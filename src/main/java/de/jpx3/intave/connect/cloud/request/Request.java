package de.jpx3.intave.connect.cloud.request;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class Request<TARGET> {
  private long lastUpdate;
  private final List<Consumer<TARGET>> subscribers = new ArrayList<>();

  public Request() {
    this.lastUpdate = System.currentTimeMillis();
  }

  public void subscribe(Consumer<TARGET> consumer) {
    subscribers.add(consumer);
    lastUpdate = System.currentTimeMillis();
  }

  public boolean publish(TARGET target) {
    subscribers.forEach(consumer -> consumer.accept(target));
    lastUpdate = System.currentTimeMillis();
    return true;
  }

  public long lastUpdate() {
    return lastUpdate;
  }
}
