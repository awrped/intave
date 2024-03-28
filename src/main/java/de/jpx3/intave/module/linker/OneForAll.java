package de.jpx3.intave.module.linker;

import java.util.function.Consumer;

public class OneForAll<SUB extends LIS_BASE, LIS_BASE, KEY> implements SubscriptionInstanceProvider<KEY, SUB, LIS_BASE> {
  private final SUB subscriber;
  private final Class<SUB> subscriberClass;

  public OneForAll(SUB subscriber) {
    this.subscriber = subscriber;
    //noinspection unchecked
    this.subscriberClass = (Class<SUB>) subscriber.getClass();
  }

  @Override
  public Class<SUB> subscriberClass() {
    return subscriberClass;
  }

  @Override
  public void apply(KEY key, Consumer<? super SUB> consumer) {
    consumer.accept(subscriber);
  }

  @Override
  public SUB fallback() {
    return subscriber;
  }
}
