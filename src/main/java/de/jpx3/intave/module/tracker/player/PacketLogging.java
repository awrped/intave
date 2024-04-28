package de.jpx3.intave.module.tracker.player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.*;
import de.jpx3.intave.IntaveControl;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.cleanup.GarbageCollector;
import de.jpx3.intave.cleanup.ShutdownTasks;
import de.jpx3.intave.module.Module;
import de.jpx3.intave.user.User;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PacketLogging extends Module {

  private final Map<UUID, PacketAdapter> adapterMap = GarbageCollector.watch(new HashMap<>());
  private final Map<String, UUID> packetLoggers = GarbageCollector.watch(new HashMap<>());
  private final Map<UUID, PrintStream> packetLogStreams = GarbageCollector.watch(new HashMap<>());

  {
    ShutdownTasks.add(() -> {
      packetLogStreams.forEach((uuid, printStream) -> {
        printStream.flush();
        printStream.close();
      });
    });
  }

  public void togglePacketLogging(CommandSender sender, Player target) {
    File logsFolder = IntaveControl.GOMME_MODE ? new File("logs") : new File(plugin.dataFolder(), "packetlogs");
    File packetLogFile = new File(logsFolder, packetLogFileName(target.getName()));

    UUID userId = target.getUniqueId();
    if (packetLoggers.containsKey(sender.getName())) {
      sender.sendMessage(IntavePlugin.prefix() + ChatColor.GREEN + "Packetlogging stopped");
//      sender.sendMessage(IntavePlugin.prefix() + "Type /intave diagnostics packetlogupload to upload the log");
      PacketAdapter remove1 = adapterMap.remove(userId);
      ProtocolLibrary.getProtocolManager().removePacketListener(remove1);
      packetLoggers.remove(sender.getName());
      PrintStream remove = packetLogStreams.remove(userId);
      if (remove != null) {
        remove.flush();
        remove.close();
      }
      return;
    }

    try {
      logsFolder.mkdir();
      packetLogFile.createNewFile();
    } catch (IOException exception) {
      exception.printStackTrace();
      return;
    }

    try {
      OutputStream stream = new FileOutputStream(packetLogFile);
      stream = new BufferedOutputStream(stream);
      PrintStream printStream = new PrintStream(stream);

      PacketAdapter adapter = new PacketAdapter(IntavePlugin.singletonInstance(), ListenerPriority.MONITOR, PacketType.values(), ListenerOptions.SKIP_PLUGIN_VERIFIER) {
        @Override
        public void onPacketSending(PacketEvent event) {
          if (event.getPlayer().getUniqueId().equals(userId)) {
            synchronized (printStream) {
              printStream.println((System.currentTimeMillis() % 1000) + " --> " + event.getPacketType().name() + (event.isCancelled() ? " (cancelled)" : "") + " " + packetContent(event.getPacket()));
            }
          }
        }

        @Override
        public void onPacketReceiving(PacketEvent event) {
          if (event.getPlayer().getUniqueId().equals(userId)) {
            synchronized (printStream) {
              printStream.println((System.currentTimeMillis() % 1000) + " <-- " + event.getPacketType().name() + (event.isCancelled() ? " (cancelled)" : "") + " " + packetContent(event.getPacket()));
            }
          }
        }
      };
      adapterMap.put(userId, adapter);
      ProtocolLibrary.getProtocolManager().addPacketListener(adapter);

      packetLoggers.put(sender.getName(), userId);
      packetLogStreams.put(userId, printStream);

    } catch (FileNotFoundException exception) {
      exception.printStackTrace();
    }
    sender.sendMessage(IntavePlugin.prefix() + ChatColor.GREEN + "Packetlogging started for " + target.getName());
    sender.sendMessage(IntavePlugin.prefix() + "You can find it under " + packetLogFile.getAbsolutePath());
  }

  public void logSystemMessage(User target, Supplier<String> messageSupplier) {
    if (target == null) {
      return;
    }
    PrintStream stream;
    try {
      stream = packetLogStreams.get(target.player().getUniqueId());
      if (stream == null) {
        return;
      }
    } catch (Exception exception) {
      return;
    }
    synchronized (stream) {
      stream.println((System.currentTimeMillis() % 1000) + " " + messageSupplier.get());
    }
  }

  private static String packetContent(PacketContainer packet) {
    if (packet == null) {
      return "null";
    }
    String contents = packet.getModifier().getValues().stream()
      .map(PacketLogging::stringFromType)
      .filter(s -> !s.isEmpty())
      .collect(Collectors.joining(", "));
    return "{" + contents + "}";
  }

  private static String stringFromType(Object object) {
    if (object == null) {
      return "null";
    } else if (object instanceof Number) {
      return object.toString();
    } else if (object instanceof String) {
      return "\"" + object + "\"";
    } else if (object instanceof Boolean) {
      return object.toString();
    } else if (object instanceof byte[]) {
      byte[] bytes = (byte[]) object;
      if (bytes.length == 0) {
        return "[]";
      } else {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int limit = Math.min(bytes.length, 40);
        for (int i = 0; i < limit; i++) {
          builder.append(bytes[i]);
          if (i != limit - 1) {
            builder.append(", ");
          }
        }
        if (bytes.length > 40) {
          builder.append("...");
        }
        builder.append("]");
        return builder.toString();
      }
    } else if (object instanceof int[]) {
      int[] ints = (int[]) object;
      if (ints.length == 0) {
        return "[]";
      } else {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int limit = Math.min(ints.length, 40);
        for (int i = 0; i < limit; i++) {
          builder.append(ints[i]);
          if (i != limit - 1) {
            builder.append(", ");
          }
        }
        if (ints.length > 40) {
          builder.append("...");
        }
        builder.append("]");
        return builder.toString();
      }
    } else if (object instanceof Object[]) {
      Object[] objects = (Object[]) object;
      if (objects.length == 0) {
        return "[]";
      } else {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int limit = Math.min(objects.length, 40);
        for (int i = 0; i < limit; i++) {
          builder.append(stringFromType(objects[i]));
          if (i != limit - 1) {
            builder.append(", ");
          }
        }
        if (objects.length > 40) {
          builder.append("...");
        }
        builder.append("]");
        return builder.toString();
      }
    } else if (object instanceof Collection) {
      Collection<?> collection = (Collection<?>) object;
      if (collection.isEmpty()) {
        return "[]";
      } else {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        int limit = Math.min(collection.size(), 40);
        int i = 0;
        for (Object o : collection) {
          builder.append(stringFromType(o));
          if (i != limit - 1) {
            builder.append(", ");
          }
          i++;
        }
        if (collection.size() > 40) {
          builder.append("...");
        }
        builder.append("]");
        return builder.toString();
      }
    } else if (object instanceof Map) {
      Map<?, ?> map = (Map<?, ?>) object;
      if (map.isEmpty()) {
        return "{}";
      } else {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        int limit = Math.min(map.size(), 40);
        int i = 0;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
          builder.append(stringFromType(entry.getKey()));
          builder.append("=");
          builder.append(stringFromType(entry.getValue()));
          if (i != limit - 1) {
            builder.append(", ");
          }
          i++;
        }
        if (map.size() > 40) {
          builder.append("...");
        }
        builder.append("}");
        return builder.toString();
      }
    } else if (object.toString().contains("DataWatcher@")) {
      //      WrappedDataWatcher watcher = new WrappedDataWatcher(object);
      //      return "DataWatcher{" + watcher.getWatchableObjects().stream().map(watchableObject -> {
      //        String value = stringFromType(watchableObject.getValue());
      //        return watchableObject.getIndex() + "=" + value;
      //      }).collect(Collectors.joining(", ")) + "}";
      return "DataWatcher{...}";
    } else if (object.toString().contains("WatchableObject@")) {
      //      WrappedDataWatcher.WrappedDataWatcherObject watcherObject = new WrappedDataWatcher.WrappedDataWatcherObject(object);
      //      return "WatchableObject{" + watcherObject.getIndex() + "=" + stringFromType(watcherObject.getHandle()) + "}";
      return "WatchableObject{...}";
    } else {
      return object.toString();
    }
  }

  private static final DateTimeFormatter FILE_MESSAGE_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy-HH-mm-ss");

  private static String packetLogFileName(String playername) {
    return "intave-packetlog-" + playername + "-" + LocalDateTime.now().format(FILE_MESSAGE_DATE_FORMATTER).toLowerCase(Locale.ROOT) + ".txt";
  }
}
