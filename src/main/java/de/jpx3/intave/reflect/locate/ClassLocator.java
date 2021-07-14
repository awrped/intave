package de.jpx3.intave.reflect.locate;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClassLocator {
  private final static ClassLocationFileCompiler fileCompiler = new ClassLocationFileCompiler();
  private final static ClassLocations classLocations = fileCompiler.fromResource("/mappings/class-locate").filterByCurrentVersion();
  private final static Map<String, ClassLocation> keyClassAccessCache = new ConcurrentHashMap<>();

  public static String patchyConvert(String input) {
    input = input.replace("/", ".");
    String output;
    if (input.startsWith("net.minecraft.server.v")) {
      output = pathByKey(input.split("\\.")[4]);
    } else {
      output = input;
    }
    return output.replace(".", "/");
  }

  public static Class<?> byFullClassName(String name) {
    if (name.startsWith("net.minecraft.server.v")) {
      return classByKey(name.split("\\.")[4]);
    } else {
      try {
        return Class.forName(name);
      } catch (ClassNotFoundException e) {
        throw new IllegalArgumentException("Unsupported class " + name);
      }
    }
  }

  public static String pathByKey(String name) {
    return classLocationByKey(name).compiledLocation();
  }

  public static Class<?> classByKey(String name) {
    return classLocationByKey(name).access();
  }

  public static ClassLocation classLocationByKey(String key) {
    return keyClassAccessCache.computeIfAbsent(key, ClassLocator::classLocationLookupByKey);
  }

  private static ClassLocation classLocationLookupByKey(String key) {
    return classLocations.filterByKey(key).stream().findAny().orElseGet(() -> {
      System.out.println(key + " has no specified location");
      return ClassLocation.nmsDefaultFor(key);
    });
  }

  public static void setup() {
    // nothing!
  }

  public static void close() {
    keyClassAccessCache.clear();
  }
}
