package de.jpx3.intave.fakeplayer;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public final class FakePlayerMetaDataHelper {
  private final static int SPRINT_BYTE = 3;
  private final static int SNEAK_BYTE = 1;
  private final static ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

  public static void setSprinting(
    Player player,
    FakePlayer fakePlayer,
    boolean sprinting
  ) {
    WrappedDataWatcher wrappedDataWatcher = fakePlayer.wrappedDataWatcher();
    List<WrappedWatchableObject> watchableObjects = wrappedDataWatcher.getWatchableObjects();
    for (WrappedWatchableObject watchableObject : watchableObjects) {
      if (watchableObject.getIndex() != 0) {
        continue;
      }
      boolean reallySneaking = (wrappedDataWatcher.getByte(0) & 1 << SPRINT_BYTE) != 0;
      if (reallySneaking != sprinting) {
        byte b0 = wrappedDataWatcher.getByte(0);
        byte value = sprinting ? (byte) (b0 | 1 << SPRINT_BYTE) : (byte) (b0 & (~(1 << SPRINT_BYTE)));
        watchableObject.setValue(value);
      }
    }
    updateMetaData(player, fakePlayer, watchableObjects);
  }

  public static void setSneaking(
    Player player,
    FakePlayer fakePlayer,
    boolean sneaking
  ) {
    WrappedDataWatcher wrappedDataWatcher = fakePlayer.wrappedDataWatcher();
    List<WrappedWatchableObject> watchableObjects = wrappedDataWatcher.getWatchableObjects();
    for (WrappedWatchableObject watchableObject : watchableObjects) {
      if (watchableObject.getIndex() != 0) {
        continue;
      }
      boolean reallySneaking = (wrappedDataWatcher.getByte(0) & 1 << SNEAK_BYTE) != 0;
      if (reallySneaking != sneaking) {
        byte b0 = wrappedDataWatcher.getByte(0);
        byte value = sneaking ? (byte) (b0 | 1 << SNEAK_BYTE) : (byte) (b0 & (~(1 << SNEAK_BYTE)));
        watchableObject.setValue(value);
      }
    }
    updateMetaData(player, fakePlayer, watchableObjects);
  }

  public static void updateHealthFor(
    Player player,
    FakePlayer fakePlayer,
    float newHealth
  ) {
    WrappedDataWatcher wrappedDataWatcher = fakePlayer.wrappedDataWatcher();
    List<WrappedWatchableObject> watchableObjects = wrappedDataWatcher.getWatchableObjects();
    for (WrappedWatchableObject watchableObject : watchableObjects) {
      if (watchableObject.getIndex() != 6) {
        continue;
      }
      watchableObject.setValue(newHealth);
    }
    updateMetaData(player, fakePlayer, watchableObjects);
  }

  private static void updateMetaData(
    Player player, FakePlayer fakePlayer,
    List<WrappedWatchableObject> watchableObjects
  ) {
    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
    packet.getIntegers().writeSafely(0, fakePlayer.fakePlayerEntityId());
    packet.getWatchableCollectionModifier().writeSafely(0, watchableObjects);
    packet.getBooleans().writeSafely(0, true);
    try {
      protocolManager.sendServerPacket(player, packet);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }
}