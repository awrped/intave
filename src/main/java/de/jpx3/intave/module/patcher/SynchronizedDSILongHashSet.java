package de.jpx3.intave.module.patcher;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;

public final class SynchronizedDSILongHashSet extends LongOpenHashSet {
  public SynchronizedDSILongHashSet() {
    super();
  }

  @Override
  public synchronized boolean add(long value) {
    return super.add(value);
  }

  @Override
  public synchronized boolean remove(long value) {
    return super.remove(value);
  }

  @Override
  public synchronized boolean contains(long value) {
    return super.contains(value);
  }
}
