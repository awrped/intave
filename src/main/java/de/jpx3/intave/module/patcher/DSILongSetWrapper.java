package de.jpx3.intave.module.patcher;

import it.unimi.dsi.fastutil.longs.AbstractLongSet;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;

public final class DSILongSetWrapper extends AbstractLongSet {
  private LongSet set;

  public DSILongSetWrapper(LongSet set) {
    this.set = set;
  }

  @Override
  public synchronized LongIterator iterator() {
    return set.iterator();
  }

  @Override
  public synchronized int size() {
    return set.size();
  }

  @Override
  public synchronized boolean contains(long l) {
    return set.contains(l);
  }

  @Override
  public synchronized boolean add(long l) {
    return set.add(l);
  }

  @Override
  public synchronized boolean remove(long l) {
    return set.remove(l);
  }

  @Override
  public synchronized void clear() {
    set.clear();
  }

  @Override
  public synchronized boolean isEmpty() {
    return set.isEmpty();
  }

  @Override
  public synchronized boolean equals(Object o) {
    return set.equals(o);
  }

  @Override
  public synchronized int hashCode() {
    return set.hashCode();
  }
}
