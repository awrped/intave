package de.jpx3.intave.module.linker.bukkit;

import de.jpx3.intave.user.User;

public interface PlayerBukkitEventSubscriber extends BukkitEventSubscriber {
  BukkitEventSubscriber bukkitSubscriberFor(User user);
}
