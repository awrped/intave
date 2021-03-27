package de.jpx3.intave.fakeplayer.equipment;

import de.jpx3.intave.tools.annotate.Nullable;
import org.bukkit.Material;

import java.util.List;

public final class EquipmentContainer {
  private final List<ArmorContext> equipment;
  @Nullable
  private final Material optionalHeldItem;

  public EquipmentContainer(List<ArmorContext> equipment, @Nullable Material optionalHeldItem) {
    this.equipment = equipment;
    this.optionalHeldItem = optionalHeldItem;
  }

  public List<ArmorContext> equipment() {
    return this.equipment;
  }

  @Nullable
  public Material heldItem() {
    return optionalHeldItem;
  }
}