package de.jpx3.intave.tools;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.Map;

public final class GarbageCollector {
  private final static List<Map<?, ?>> boundMaps = Lists.newCopyOnWriteArrayList();

  private GarbageCollector() {
    throw new UnsupportedOperationException();
  }

  // class loading
  public static void setup() {

  }

  public static <K, V> Map<K, V> watch(Map<K, V> initialMap) {
    boundMaps.add(initialMap);
    return initialMap;
  }

  public static <K> void clear(K key) {
    boundMaps.forEach(boundList -> boundList.remove(key));
  }

  public static void die() {
    boundMaps.forEach(Map::clear);
    boundMaps.clear();
  }
}
