package de.jpx3.intave.module.linker;

import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserRepository;
import org.bukkit.entity.Player;

import java.util.function.Consumer;
import java.util.function.Function;

public class OneForOne<T extends LISTENER, LISTENER> implements SubscriptionInstanceProvider<User, T, LISTENER> {
  private final Function<? super User, ? extends T> supplier;
  private final T fallback;
  private final Class<T> subscriberClass;

  public OneForOne(Function<? super User, ? extends T> supplier) {
    this.supplier = supplier;
    this.fallback = supplier.apply(UserRepository.userOf((Player) null));
    this.subscriberClass = (Class<T>) fallback.getClass();
  }

  @Override
  public Class<T> subscriberClass() {
    return subscriberClass;
  }

  @Override
  public void apply(User user, Consumer<? super T> consumer) {
    consumer.accept(supplier.apply(user));
  }

  @Override
  public T fallback() {
    return fallback;
  }
}
