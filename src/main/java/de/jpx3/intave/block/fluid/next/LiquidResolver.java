package de.jpx3.intave.block.fluid.next;

import org.bukkit.Material;

interface LiquidResolver {
  Liquid liquidFrom(Material type, int variantIndex);

  default Liquid select(
    boolean isWater,
    boolean isLava,
    boolean dry,
    float height,
    boolean source
  ) {
    if (dry) {
      return Dry.of();
    } else if (isWater) {
      return source ? StationaryWater.of() : FlowingWater.ofHeight(height);
    } else if (isLava) {
      return source ? StationaryLava.of() : FlowingLava.ofHeight(height);
    }
    return Dry.of();
  }
}
