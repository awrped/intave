package de.jpx3.intave.fakeplayer.equipment;

import de.jpx3.intave.tools.annotate.Nullable;
import org.bukkit.Material;

public final class ArmorContext {
  private final Equipment.Type type;
  @Nullable
  private final Material armorMaterial;

  public ArmorContext(Equipment.Type type, @Nullable Material armorMaterial) {
    this.type = type;
    this.armorMaterial = armorMaterial;
  }

  public Equipment.Type type() {
    return type;
  }

  @Nullable
  public Material armorMaterial() {
    return armorMaterial;
  }
}
