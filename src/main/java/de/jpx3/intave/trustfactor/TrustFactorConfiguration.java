package de.jpx3.intave.trustfactor;

import de.jpx3.intave.access.player.trust.TrustFactor;

public interface TrustFactorConfiguration {
  int resolveSetting(String key, TrustFactor trustFactor);
}
