package de.jpx3.intave.fakeplayer.equipment;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.Material;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public final class Equipment {
  private final static Map<Type, Set<Material>> equipment = Maps.newHashMap();
  private final static List<Material> heldItems = ImmutableList.of(
    Material.WOOD_SWORD,
    Material.GOLD_SWORD,
    Material.STONE_SWORD,
    Material.IRON_SWORD,
    Material.DIAMOND_SWORD,
    Material.WOOD_AXE,
    Material.GOLD_AXE,
    Material.STONE_AXE,
    Material.IRON_AXE,
    Material.DIAMOND_AXE,
    Material.WOOD_PICKAXE,
    Material.GOLD_PICKAXE,
    Material.STONE_PICKAXE,
    Material.IRON_PICKAXE,
    Material.DIAMOND_PICKAXE
  );

  static {
    equipment.put(Type.HELMET, ImmutableSet.of(
      Material.LEATHER_HELMET,
      Material.CHAINMAIL_HELMET,
      Material.IRON_HELMET,
      Material.DIAMOND_HELMET
    ));
    equipment.put(Type.CHESTPLATE, ImmutableSet.of(
      Material.LEATHER_CHESTPLATE,
      Material.CHAINMAIL_CHESTPLATE,
      Material.IRON_CHESTPLATE,
      Material.DIAMOND_CHESTPLATE
    ));
    equipment.put(Type.LEGGINGS, ImmutableSet.of(
      Material.LEATHER_LEGGINGS,
      Material.CHAINMAIL_LEGGINGS,
      Material.IRON_LEGGINGS,
      Material.DIAMOND_LEGGINGS
    ));
    equipment.put(Type.BOOTS, ImmutableSet.of(
      Material.LEATHER_BOOTS,
      Material.CHAINMAIL_BOOTS,
      Material.IRON_BOOTS,
      Material.DIAMOND_BOOTS
    ));
  }

  public static EquipmentContainer createEquipment() {
    List<ArmorContext> armorContextList = Lists.newArrayList();
    for (Type value : Type.values()) {
      ArmorContext randomArmorContext = createRandomArmorContext(value);
      armorContextList.add(randomArmorContext);
    }
    return new EquipmentContainer(armorContextList, randomHeldItem().orElse(Material.AIR));
  }

  private static ArmorContext createRandomArmorContext(
    Type equipmentType
  ) {
    Material equipmentForType = findEquipmentForType(equipmentType);
    return new ArmorContext(equipmentType, equipmentForType);
  }

  @Nullable
  private static Material findEquipmentForType(
    Type equipmentType
  ) {
    int id = ThreadLocalRandom.current().nextInt(0, 5);
    List<Material> materials = listForEquipmentType(equipmentType);
    if (id > materials.size() - 1) {
      return null;
    }
    return materials.get(id);
  }

  private static List<Material> listForEquipmentType(
    Type equipmentType
  ) {
    List<Material> materials = Lists.newArrayList();
    for (Map.Entry<Type, Set<Material>> typeSetEntry : equipment.entrySet()) {
      if (typeSetEntry.getKey() != equipmentType) {
        continue;
      }
      materials.addAll(typeSetEntry.getValue());
    }
    return materials;
  }

  private static Optional<Material> randomHeldItem() {
    int id = ThreadLocalRandom.current().nextInt(0, heldItems.size() + 3);
    if (id > heldItems.size() - 1) {
      return Optional.empty();
    }
    Material material = heldItems.get(id);
    return Optional.of(material);
  }

  public enum Type {
    HELMET(5),
    CHESTPLATE(4),
    LEGGINGS(3),
    BOOTS(2);

    private final int slotId;

    Type(int slotId) {
      this.slotId = slotId;
    }

    public int slotId() {
      return this.slotId;
    }
  }
}