package de.jpx3.intave.user;

import de.jpx3.intave.cleanup.GarbageCollector;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class UserLocal<T> {
  private final Initializer<T> initializer;
  private final Map<UUID, T> map = GarbageCollector.watch(new ConcurrentHashMap<>());

  private UserLocal(Initializer<T> initializer) {
    this.initializer = initializer;
  }

  public T get(User user) {
    if (!user.hasPlayer()) {
      return initializer.initialize();
    }
    UUID id = user.player().getUniqueId();
    return map.computeIfAbsent(id, uuid -> initializer.initialize());
  }

  public static <T> UserLocal<T> withInitial(Initializer<T> initializer) {
    return new UserLocal<>(initializer);
  }

  public interface Initializer<T> {
    T initialize();
  }
}
