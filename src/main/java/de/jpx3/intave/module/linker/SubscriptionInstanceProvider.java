package de.jpx3.intave.module.linker;

import java.util.function.Consumer;

public interface SubscriptionInstanceProvider<KEY, TYPE extends LISTENER, LISTENER> {
  Class<TYPE> subscriberClass();

  void apply(KEY key, Consumer<? super TYPE> consumer);

  TYPE fallback();
}
