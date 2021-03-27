package de.jpx3.intave.fakeplayer.randomaction.actions;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import de.jpx3.intave.fakeplayer.FakePlayer;
import de.jpx3.intave.fakeplayer.equipment.ArmorContext;
import de.jpx3.intave.fakeplayer.equipment.Equipment;
import de.jpx3.intave.fakeplayer.equipment.EquipmentContainer;
import de.jpx3.intave.fakeplayer.randomaction.ActionType;
import de.jpx3.intave.fakeplayer.randomaction.RandomAction;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public final class EquipmentAction extends RandomAction {
  public EquipmentAction(Player player, FakePlayer fakePlayer) {
    super(Probability.LOW, ActionType.EQUIPMENT, player, fakePlayer);
  }

  @Override
  protected void performAction() {
    EquipmentContainer equipment = Equipment.createEquipment();
    List<ArmorContext> armorContextList = equipment.equipment();
    for (ArmorContext armorContext : armorContextList) {
      Material armorMaterial = armorContext.armorMaterial();
      if (armorMaterial == null) {
        continue;
      }
      int slotId = armorContext.type().slotId();
      sendEquipment(slotId - 1, armorMaterial);
    }
    Material optionalHeldItem = equipment.heldItem();
    if (optionalHeldItem != null) {
      sendEquipment(0, optionalHeldItem);
    }
  }

  private void sendEquipment(
    int slot,
    Material material
  ) {
    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_EQUIPMENT);
    packet.getIntegers().writeSafely(0, this.fakePlayer.fakePlayerEntityId());
    packet.getModifier().writeSafely(1, slot);
    packet.getItemModifier().writeSafely(0, new ItemStack(material));
    try {
      protocolManager.sendServerPacket(this.parentPlayer, packet);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }
}