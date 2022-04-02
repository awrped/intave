package de.jpx3.intave.module.patcher;

import org.bukkit.craftbukkit.v1_8_R3.util.LongHashSet;

public final class SynchronizedBukkitLongHashSet extends LongHashSet {
  @Override
  public synchronized boolean add(long l) {
    return super.add(l);
  }

  @Override
  public synchronized boolean contains(long l) {
    return super.contains(l);
  }

  @Override
  public synchronized void clear() {
    super.clear();
  }

  @Override
  public synchronized boolean remove(long l) {
    return super.remove(l);
  }
}
