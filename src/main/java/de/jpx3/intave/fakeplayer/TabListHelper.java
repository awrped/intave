package de.jpx3.intave.fakeplayer;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.PlayerInfoData;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedGameProfile;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public final class TabListHelper {
  private final static ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

  public static void addToTabList(
    Player player,
    WrappedGameProfile wrappedGameProfile,
    String tabListName
  ) {
    WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromText(tabListName);
    addToTabList(player, wrappedGameProfile, wrappedChatComponent);
  }

  private static void addToTabList(
    Player player,
    WrappedGameProfile wrappedGameProfile,
    WrappedChatComponent wrappedChatComponent
  ) {
    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
    PlayerInfoData playerInfoData = new PlayerInfoData(
      wrappedGameProfile,
      ThreadLocalRandom.current().nextInt(20, 200),
      EnumWrappers.NativeGameMode.SURVIVAL,
      wrappedChatComponent
    );
    List<PlayerInfoData> playerInformationList = packet.getPlayerInfoDataLists().readSafely(0);
    playerInformationList.add(playerInfoData);
    packet.getPlayerInfoAction().writeSafely(0, EnumWrappers.PlayerInfoAction.ADD_PLAYER);
    packet.getPlayerInfoDataLists().writeSafely(0, playerInformationList);
    try {
      protocolManager.sendServerPacket(player, packet);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  public static void removeFromTabList(
    Player player,
    WrappedGameProfile wrappedGameProfile
  ) {
    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO);
    WrappedChatComponent wrappedChatComponent = WrappedChatComponent.fromText(wrappedGameProfile.getName());
    PlayerInfoData playerInfoData = new PlayerInfoData(
      wrappedGameProfile,
      ThreadLocalRandom.current().nextInt(20, 200),
      EnumWrappers.NativeGameMode.SURVIVAL,
      wrappedChatComponent
    );
    List<PlayerInfoData> playerInformationList = packet.getPlayerInfoDataLists().readSafely(0);
    playerInformationList.add(playerInfoData);
    packet.getPlayerInfoAction().writeSafely(0, EnumWrappers.PlayerInfoAction.REMOVE_PLAYER);
    packet.getPlayerInfoDataLists().writeSafely(0, playerInformationList);
    try {
      protocolManager.sendServerPacket(player, packet);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }
}
