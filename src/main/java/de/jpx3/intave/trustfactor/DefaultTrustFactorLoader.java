package de.jpx3.intave.trustfactor;

import de.jpx3.intave.IntavePlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;

public final class DefaultTrustFactorLoader implements TrustFactorLoader {
  @Override
  public TrustFactorConfiguration fetch() {
    String fileName = "/" + IntavePlugin.version().replace(".", "-") + ".yml";
    InputStream resourceAsStream = getClass().getResourceAsStream(fileName);
    return new YamlTrustFactorConfiguration(YamlConfiguration.loadConfiguration(new InputStreamReader(resourceAsStream)));
  }
}
