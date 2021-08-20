package de.jpx3.intave.cleanup;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Function;

public final class ReferenceMap<K, V> implements Map<K, V> {
  private final Map<K, Reference<V>> map;
  private final Function<V, Reference<V>> referencer;

  private ReferenceMap(
    Map<K, Reference<V>> map,
    Function<V, Reference<V>> referencer
  ) {
    this.map = map;
    this.referencer = referencer;
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    for (Reference<V> vReference : map.values()) {
      V innerValue = vReference.get();
      if (innerValue != null && (value == innerValue || value.equals(innerValue))) {
        return true;
      }
    }
    return false;
  }

  @Override
  public V get(Object key) {
    Reference<V> reference = map.get(key);
    return reference == null ? null : reference.get();
  }

  @Nullable
  @Override
  public V put(K key, V value) {
    Reference<V> oldValue = map.put(key, referencer.apply(value));
    return oldValue == null ? null : oldValue.get();
  }

  @Override
  public V remove(Object key) {
    Reference<V> oldValue = map.remove(key);
    return oldValue == null ? null : oldValue.get();
  }

  @Override
  public void putAll(@NotNull Map<? extends K, ? extends V> m) {
    m.forEach(this::put);
  }

  @Override
  public void clear() {
    map.clear();
  }

  @NotNull
  @Override
  public Set<K> keySet() {
    return map.keySet();
  }

  @NotNull
  @Override
  public Collection<V> values() {
    Collection<Reference<V>> values = map.values();
    List<V> newValues = new ArrayList<>(values.size());
    for (Reference<V> value : values) {
      newValues.add(value.get());
    }
    return newValues;
  }

  @NotNull
  @Override
  public Set<Entry<K, V>> entrySet() {
    Set<Entry<K, Reference<V>>> entries = map.entrySet();
    Set<Entry<K, V>> newEntries = new HashSet<>();
    for (Entry<K, Reference<V>> entry : entries) {
      newEntries.add(new Entry<K, V>() {
        @Override
        public K getKey() {
          return entry.getKey();
        }

        @Override
        public V getValue() {
          return entry.getValue().get();
        }

        @Override
        public V setValue(V value) {
          Reference<V> oldValue = entry.setValue(referencer.apply(value));
          return oldValue == null ? null : oldValue.get();
        }
      });
    }
    return newEntries;
  }

  public static <K, V> ReferenceMap<K, V> soft(Map<K, Reference<V>> map) {
    return new ReferenceMap<>(map, SoftReference::new);
  }

  public static <K, V> ReferenceMap<K, V> weak(Map<K, Reference<V>> map) {
    return new ReferenceMap<>(map, WeakReference::new);
  }
}
