package de.jpx3.intave.module;

import org.bukkit.Bukkit;

import java.util.Arrays;

public final class PluginRequirement implements Requirement {
  private final String[] dependencies;

  public PluginRequirement(String... dependencies) {
    this.dependencies = dependencies;
  }

  @Override
  public boolean fulfilled() {
    if (dependencies == null || dependencies.length == 0) {
      return true;
    }
    return Arrays.stream(dependencies)
      .allMatch(dependency -> Bukkit.getPluginManager().isPluginEnabled(dependency));
  }
}
