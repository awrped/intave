package de.jpx3.intave.tools.packet;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.reflect.StructureModifier;
import com.google.common.collect.Maps;
import de.jpx3.intave.access.IntaveInternalException;
import de.jpx3.intave.reflect.ReflectiveAccess;

import java.util.Map;

public final class PlayerActionResolver {
  private final static Map<String, PlayerAction> playerActionAccessor = Maps.newHashMap();
  private final static Class<?> PLAYER_ACTION_CLASS = ReflectiveAccess.lookupServerClass("PacketPlayInEntityAction$EnumPlayerAction");

  static {
    for (PlayerAction playerAction : PlayerAction.values()) {
      String actionName = playerAction.action();
      playerActionAccessor.put(actionName, playerAction);
    }
  }

  public static PlayerAction resolveActionFromPacket(PacketContainer packet) {
    StructureModifier<Object> playerActions = playerActionOf(packet);
    for (Object value : playerActions.getValues()) {
      PlayerAction playerAction = playerActionAccessor.get(value.toString());
      if (playerAction != null) {
        return playerAction;
      } else {
        throw new IllegalStateException("Unable to access PlayerAction: " + value);
      }
    }
    throw new IntaveInternalException("Packet does not contain PlayerAction");
  }

  private static StructureModifier<Object> playerActionOf(PacketContainer packet) {
    return packet.getModifier().withType(PLAYER_ACTION_CLASS);
  }
}