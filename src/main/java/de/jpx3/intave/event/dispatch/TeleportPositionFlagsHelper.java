package de.jpx3.intave.event.dispatch;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.comphenix.protocol.utility.MinecraftReflection;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.jpx3.intave.tools.annotate.KeepEnumInternalNames;

import java.util.Set;

final class TeleportPositionFlagsHelper {
  private static final Class<?> FLAGS_CLASS = MinecraftReflection.getMinecraftClass(
    "EnumPlayerTeleportFlags",
    "PacketPlayOutPosition$EnumPlayerTeleportFlags"
  );

  static StructureModifier<Set<PlayerTeleportFlag>> flagsModifier(PacketContainer packet) {
    return packet.getSets(EnumWrappers.getGenericConverter(FLAGS_CLASS, PlayerTeleportFlag.class));
  }

  @KeepEnumInternalNames
  public enum PlayerTeleportFlag {
    X, Y, Z, Y_ROT, X_ROT
  }
}