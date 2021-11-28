package de.jpx3.intave.check.movement.timer;

import com.comphenix.protocol.events.PacketEvent;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.check.MetaCheckPart;
import de.jpx3.intave.check.movement.Timer;
import de.jpx3.intave.math.MathHelper;
import de.jpx3.intave.module.Modules;
import de.jpx3.intave.module.linker.packet.PacketId;
import de.jpx3.intave.module.linker.packet.PacketSubscription;
import de.jpx3.intave.module.violation.Violation;
import de.jpx3.intave.module.violation.ViolationContext;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserRepository;
import de.jpx3.intave.user.meta.CheckCustomMetadata;
import de.jpx3.intave.user.meta.MovementMetadata;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Arrays;

import static de.jpx3.intave.module.linker.packet.PacketId.Client.*;

public final class MovementFrequency extends MetaCheckPart<Timer, MovementFrequency.MovementFrequencyData> {
  private final IntavePlugin plugin = IntavePlugin.singletonInstance();

  public MovementFrequency(Timer parentCheck) {
    super(parentCheck, MovementFrequencyData.class);

    setupScheduler();
  }

  private void setupScheduler() {
    Bukkit.getScheduler().scheduleSyncRepeatingTask(
      plugin,
      () -> Bukkit.getOnlinePlayers().forEach(this::transaction),
      20, 1
    );
  }

  private void transaction(Player player) {
    User user = userOf(player);
    MovementFrequencyData frequencyData = metaOf(user);

    long now = System.currentTimeMillis();

    long lastTask = frequencyData.lastTask;
    frequencyData.lastTask = now;

    // Compensate Server lag
    if (Math.abs(now - lastTask - 50) > 10) {
      frequencyData.fillAverage();
    }

    Modules.feedback().synchronize(player, (p, target) -> checkPackets(user));
  }

  private void checkPackets(User user) {
    Player player = user.player();
    MovementFrequencyData frequencyData = metaOf(user);

    long now = System.currentTimeMillis();
    long lastTransaction = frequencyData.lastTransactionPacket;
    frequencyData.lastTransactionPacket = now;

    MovementMetadata movement = user.meta().movement();
    boolean moving = movement.motion().length() > 0.1;

    if (lastTransaction == -1 || !moving) {
      return;
    }

    int[] average = frequencyData.average;
    int packetsThisTick = frequencyData.packets;

    int index = ++frequencyData.averagePointer % average.length;
    frequencyData.averagePointer = index;
    average[index] = packetsThisTick;

    double balance = 0;
    for (int i : average) {
      balance += i;
    }
    balance /= average.length;

    int offset = Math.floorMod(frequencyData.averagePointer - 1, average.length);
    double pct = 1;
    double weightedAverage = average[offset];

    for (int i = 1; i < average.length; i++) {
      int avg = average[(offset + i) % average.length];
      weightedAverage = pct * weightedAverage + (1 - pct) * avg;
    }

    double deviation = Math.abs(balance - 1);
    boolean check = frequencyData.tick - frequencyData.checkTick > Math.max(1, 100 - balance * 80);

    if (check) {
      frequencyData.checkTick = frequencyData.tick;
    }

    if (check && deviation > 0.01) {
      double multiplier = deviation * 100;

      frequencyData.vl = Math.max(1, frequencyData.vl) * multiplier;
      frequencyData.flagTick = frequencyData.tick;

      ChatColor chatColor = frequencyData.vl > 1000 ? ChatColor.RED : ChatColor.GOLD;
//       player.sendMessage(chatColor + "deviation too big:" + balance + " vl " + MathHelper.formatDouble(frequencyData.vl, 2));

      if (frequencyData.vl > 1000) {
        Violation violation = Violation.builderFor(Timer.class).forPlayer(player)
          .withMessage("moved too frequently")
          .withDetails(MathHelper.formatDouble(balance, 2) + " packet average").withVL(0.5)
          .build();
        ViolationContext violationContext = Modules.violationProcessor().processViolation(violation);
       // if (violationContext.shouldCounterThreat()) {
          MovementMetadata movementData = user.meta().movement();
          movementData.invalidMovement = true;
          Vector setback = new Vector(movementData.physicsMotionX, movementData.physicsMotionY, movementData.physicsMotionZ);
          Modules.mitigate().movement().emulationSetBack(player, setback, 12, false);
          frequencyData.fillAverage();

       // }
      }
    } else {
      double multiplier = frequencyData.tick - frequencyData.flagTick > 17 ? 0.99 : 0.985;
      frequencyData.vl *= multiplier;
    }

    frequencyData.vl = Math.min(2_000, frequencyData.vl);

    player.sendMessage(MathHelper.formatDouble(balance, 2) + " | " + packetsThisTick + " vl " + MathHelper.formatDouble(frequencyData.vl, 2));

    frequencyData.packets = 0;
    frequencyData.tick++;
  }

  @PacketSubscription(
    packetsIn = {
      POSITION_LOOK, POSITION, FLYING, LOOK
    }
  )
  public void clientTickUpdate(PacketEvent event) {
    Player player = event.getPlayer();
    User user = userOf(player);

    MovementFrequencyData frequencyData = metaOf(user);
    MovementMetadata movement = user.meta().movement();

    long now = System.currentTimeMillis();
    boolean moving = movement.motion().length() > 0.1;

    long lastFlying = frequencyData.lastFlying;
    frequencyData.lastFlying = now;

    if (lastFlying != -1 && moving) {
      frequencyData.packets++;
    }
  }

  @PacketSubscription(
    packetsOut = {
      PacketId.Server.POSITION
    }
  )
  public void catchTeleport(PacketEvent event) {
    Player player = event.getPlayer();
    User user = UserRepository.userOf(player);
    MovementFrequencyData channelActivityMeta = metaOf(user);

    Modules.feedback().synchronize(player, (h, s) -> {
      channelActivityMeta.packets--;
    });
  }

  public static final class MovementFrequencyData extends CheckCustomMetadata {
    public long lastFlying = -1;
    public long lastTransactionPacket = -1;

    public final int[] average = new int[70];
    public long lastTask = -1;
    public int averagePointer;
    public int packets;

    public double vl;
    public int tick, flagTick, checkTick, teleports;

    public MovementFrequencyData() {
      fillAverage();
    }

    public void fillAverage() {
      Arrays.fill(average, 1);
    }
  }
}