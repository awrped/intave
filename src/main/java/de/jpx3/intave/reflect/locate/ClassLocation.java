package de.jpx3.intave.reflect.locate;

import de.jpx3.intave.reflect.ReflectiveAccess;

public final class ClassLocation {
  private final String name;
  private final IntegerMatcher versionMatcher;
  private final String location;

  public ClassLocation(String name, IntegerMatcher versionMatcher, String location) {
    this.name = name;
    this.versionMatcher = versionMatcher;
    this.location = location;
  }

  public Class<?> access() {
    String className = location.replace("{version}", ReflectiveAccess.version());
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException exception) {
      throw new IllegalStateException(exception);
    }
  }

  public String compiledLocation() {
    return location.replace("{version}", ReflectiveAccess.version());
  }

  public String name() {
    return name;
  }

  public IntegerMatcher versionMatcher() {
    return versionMatcher;
  }

  public String location() {
    return location;
  }

  public static ClassLocation nmsDefaultFor(String name) {
    return new ClassLocation(name, IntegerMatcher.inRange(8, 16), "net.minecraft.server.{version}." + name);
  }
}
