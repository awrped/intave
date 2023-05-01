package de.jpx3.intave.connect.sibyl;

import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.annotate.Native;
import de.jpx3.intave.diagnostic.natives.NativeCheck;
import de.jpx3.intave.executor.Synchronizer;
import de.jpx3.intave.user.MessageChannelSubscriptions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Collection;

public final class SibylBroadcast {

  public static void setup() {
    NativeCheck.registerNative(() -> SibylBroadcast.broadcast(null));
  }

  @Native
  public static void broadcast(String message) {
    if (NativeCheck.checkActive() || message == null || message.isEmpty()) {
      return;
    }
    Collection<? extends Player> receiver = MessageChannelSubscriptions.sibylReceivers();
    if (receiver.isEmpty()) {
      return;
    }
    if (!Bukkit.isPrimaryThread()) {
      Synchronizer.synchronize(() -> broadcast(message));
      return;
    }
    IntavePlugin intavePlugin = IntavePlugin.singletonInstance();
    for (Player authenticatedPlayer : receiver) {
      if (intavePlugin.sibyl().isAuthenticated(authenticatedPlayer)) {
//        authenticatedPlayer.sendMessage(message);
        SibylMessageTransmitter.sendMessage(authenticatedPlayer, message);
      }
    }
  }
}
