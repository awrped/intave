package de.jpx3.intave.trustfactor;

import de.jpx3.intave.IntaveLogger;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.resource.Resource;
import de.jpx3.intave.resource.Resources;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

public final class InternetYamlTrustFactorLoader implements TrustFactorLoader {
  @Override
  public TrustFactorConfiguration fetch() {
    Resource trustfactor = Resources.cacheResourceChain("https://service.intave.de/trustfactor/" + IntavePlugin.version(), "trustfactor", TimeUnit.DAYS.toMillis(7));
    InputStreamReader reader = new InputStreamReader(trustfactor.read());
    YamlConfiguration configuration = YamlConfiguration.loadConfiguration(reader);
    if (configuration.getConfigurationSection("physics") == null) {
      IntaveLogger.logger().error("Unable to download TXM file");
    }
    return new YamlTrustFactorConfiguration(configuration);
  }
}
