package de.jpx3.intave.connect.sibyl;

import de.jpx3.intave.annotate.Native;
import de.jpx3.intave.executor.Synchronizer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class SibylMessageTransmitter {
  @Native
  public static void sendMessage(Player player, String encryptedFormat, String... args) {
    if (!Bukkit.isPrimaryThread()) {
      Synchronizer.synchronize(() -> sendMessage(player, encryptedFormat, args));
      return;
    }
    // for now, just send the message to the player
    player.sendMessage(String.format(encryptedFormat, (Object[]) args));
  }
}
