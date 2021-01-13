package de.jpx3.intave.command.stages;

import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.command.CommandStage;
import de.jpx3.intave.command.SubCommand;
import de.jpx3.intave.event.service.MessageFormatter;
import de.jpx3.intave.tools.placeholder.TextContext;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserMessageChannel;
import de.jpx3.intave.user.UserRepository;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.stream.Collectors;

public final class IntaveInternalsStage extends CommandStage {
  private static IntaveInternalsStage singletonInstance;
  private final IntavePlugin plugin;

  private IntaveInternalsStage() {
    super(IntaveCommandStage.singletonInstance(), "internals", 1);
    plugin = IntavePlugin.singletonInstance();
  }

  @SubCommand(
    selectors = "sendnotify",
    usage = "<message...>",
    permission = "intave.command.internals.sendnotify",
    description = "Send notifications"
  )
  public void internalCommand(CommandSender commandSender, String[] message) {
    String fullMessage = Arrays.stream(message).map(s -> s + " ").collect(Collectors.joining()).trim();
    String notifyMessage = MessageFormatter.resolveNotifyReplacements(new TextContext(fullMessage));

    for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
      User user = UserRepository.userOf(onlinePlayer);
      if(user.receives(UserMessageChannel.NOTIFY)) {
        if(user.hasChannelConstraint(UserMessageChannel.NOTIFY)) {
          if(user.channelPlayerConstraint(UserMessageChannel.NOTIFY).appliesTo(onlinePlayer)) {
            onlinePlayer.sendMessage(notifyMessage);
          }
        } else {
          onlinePlayer.sendMessage(notifyMessage);
        }
      }
    }
  }

  public static IntaveInternalsStage singletonInstance() {
    if(singletonInstance == null) {
      singletonInstance = new IntaveInternalsStage();
    }
    return singletonInstance;
  }
}
