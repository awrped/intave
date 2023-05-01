package de.jpx3.intave.module.actionbar;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.jpx3.intave.check.EventProcessor;
import de.jpx3.intave.check.combat.clickpatterns.Kurtosis;
import de.jpx3.intave.module.linker.packet.ListenerPriority;
import de.jpx3.intave.module.linker.packet.PacketSubscription;
import de.jpx3.intave.packet.reader.EntityUseReader;
import de.jpx3.intave.packet.reader.PacketReaders;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserLocal;
import de.jpx3.intave.user.UserRepository;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;

import static com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType.DROP_ITEM;
import static de.jpx3.intave.math.MathHelper.formatDouble;
import static de.jpx3.intave.module.linker.packet.PacketId.Client.*;
import static java.lang.Math.pow;

public final class ClickFeeder implements EventProcessor {
  private final UserLocal<ClickBufferData> bufferData = UserLocal.withInitial(ClickBufferData::new);

  @PacketSubscription(
    priority = ListenerPriority.HIGH,
    packetsIn = {
      USE_ENTITY, ARM_ANIMATION, BLOCK_DIG
    }
  )
  public void clientClickUpdate(PacketEvent event) {
    Player player = event.getPlayer();
    User user = UserRepository.userOf(player);
    ClickBufferData bufferData = this.bufferData.get(user);
    PacketContainer packet = event.getPacket();
    PacketType type = packet.getType();
    if (type == PacketType.Play.Client.USE_ENTITY) {
      EntityUseReader reader = PacketReaders.readerOf(packet);
      EnumWrappers.EntityUseAction entityUseAction = reader.useAction();
      if (entityUseAction == EnumWrappers.EntityUseAction.ATTACK) {
        bufferData.attacks++;
      }
      reader.release();
    } else if (type == PacketType.Play.Client.ARM_ANIMATION) {
      bufferData.clicks++;
    } else if (type == PacketType.Play.Client.BLOCK_DIG) {
      if (packet.getPlayerDigTypes().read(0) == DROP_ITEM && user.meta().inventory().heldItemType() == Material.AIR) {
        UUID actionTarget = user.actionTarget();
        if (actionTarget != null) {
          User actionTargetUser = UserRepository.userOf(actionTarget);
          if (actionTargetUser.hasPlayer()) {
            ClickBufferData otherBufferData = this.bufferData.get(actionTargetUser);
            otherBufferData.tab++;
            otherBufferData.tab %= 3;
            otherBufferData.frontVisible = 0;
          }
        }
      } else {
        bufferData.breakingBlock = user.meta().attack().inBreakProcess;
        bufferData.places++;
      }
    }
  }

  @PacketSubscription(
    priority = ListenerPriority.HIGH,
    packetsIn = {
      FLYING, LOOK, POSITION, POSITION_LOOK
    }
  )
  public void clientTickUpdate(PacketEvent event) {
    Player player = event.getPlayer();
    User user = UserRepository.userOf(player);

    ClickBufferData bufferData = this.bufferData.get(user);
    TickAction action = TickAction.NOTHING;
    int intensity = 0;

    if (bufferData.clicks > 0) {
      action = TickAction.CLICK;
      intensity = bufferData.clicks;
    }
    if (bufferData.attacks > 0) {
      action = TickAction.ATTACK;
      intensity = bufferData.attacks;
    } else if (bufferData.places > 0) {
      action = TickAction.PLACE;
      intensity = bufferData.places;
    }
    if (user.anyActionSubscriptions()) {
      bufferData.append(action, intensity);
      String text;
      if (bufferData.mainVisible < 15) {
        text = ChatColor.GRAY + "Intave Recordbar Display";
      } else if (bufferData.mainVisible < 30) {
        text = ChatColor.GRAY + "Cycle with Q on empty hand";
      } else /*if (bufferData.frontVisible < 3) {
        text = ChatColor.GRAY + "Display change";
      } else*/ if ((bufferData.historyVisible > 0 || bufferData.frontVisible == 0) && bufferData.tab == 1 && bufferData.historyVisible < 10) {
        text = ChatColor.GRAY + "C = Clicks, A = Attacks, P = Places, " + ChatColor.GREEN + "Once per tick" + ChatColor.GRAY + ", " + ChatColor.YELLOW + "Twice per tick" + ChatColor.GRAY + ", " + ChatColor.RED + "Three times per tick";
        bufferData.frontVisible = 1;
      } else {
        text = bufferData.buildActionBar();
      }
      user.pushActionDisplayToSubscribers(DisplayType.CLICKS, text);
      bufferData.mainVisible++;
      bufferData.frontVisible++;
      if (bufferData.mainVisible >= 30) {
        if (bufferData.tab == 0) {
          bufferData.statsVisible = 0;
          bufferData.historyVisible = 0;
          bufferData.reducedVisible++;
        } else if (bufferData.tab == 1) {
          bufferData.reducedVisible = 0;
          bufferData.statsVisible = 0;
          bufferData.historyVisible++;
        } else if (bufferData.tab == 2) {
          bufferData.reducedVisible = 0;
          bufferData.historyVisible = 0;
          bufferData.statsVisible++;
        }
      }
    } else {
      bufferData.mainVisible = 0;
    }
    bufferData.attacks = 0;
    bufferData.clicks = 0;
    bufferData.places = 0;
  }

  public static class ClickBufferData {
    private final User user;
    private final List<TickAction> tickActions = new LinkedList<>();
    private final List<Integer> tickIntensity = new LinkedList<>();
    private final List<Boolean> inBlockBreak = new LinkedList<>();
    private int clicks, attacks, places;
    private boolean breakingBlock;
    private int mainVisible = 0;
    private int frontVisible = 0;
    private int reducedVisible = 0;
    private int historyVisible = 0;
    private int statsVisible = 0;
    private int tab = 0;

    {
      for (int i = 0; i < 40; i++) {
        tickActions.add(TickAction.NOTHING);
        tickIntensity.add(0);
        inBlockBreak.add(false);
      }
    }

    public ClickBufferData(User user) {
      this.user = user;
    }

    public synchronized void append(TickAction action, int intensity) {
      tickActions.remove(0);
      tickActions.add(action);
      tickIntensity.remove(0);
      tickIntensity.add(intensity);
      inBlockBreak.remove(0);
      inBlockBreak.add(breakingBlock);
    }

    public String buildActionBar() {
      int attackTicks = 0, clickTicks = 0;
      int whileBreaking = 0;

      for (int i = tickActions.size() - 1; i >= tickIntensity.size() / 2; i--) {
        TickAction tickAction = tickActions.get(i);
        if (tickAction == TickAction.ATTACK) {
          attackTicks++;
          clickTicks++;
        }
        if (tickAction == TickAction.CLICK) {
          clickTicks++;
        }
        if (inBlockBreak.get(i)) {
          whileBreaking++;
        }
      }

      StringBuilder builder = new StringBuilder();
      builder.append("&c");
      builder.append(user.player().getName());
      builder.append(" &7| ");
      if (attackTicks == 0 && clickTicks == 0 && (frontVisible / 20) % 2 == 0 && frontVisible <= 60) {
        builder.append("A/C");
      } else {
        boolean breakBlock = whileBreaking > 5;
        if (breakingBlock) {
//          builder.append(ChatColor.STRIKETHROUGH);
        }
        builder.append(attackTicks);
        if (breakingBlock) {
          builder.append(ChatColor.GRAY);
        }
        builder.append("/");
        if (breakingBlock) {
          builder.append(ChatColor.STRIKETHROUGH);
        }
        builder.append(clickTicks);
        if (breakingBlock) {
          builder.append(ChatColor.GRAY);
        }
      }

      if (tab == 1) {
        builder.append(" | ");
        for (int i = tickIntensity.size() - 1; i >= 0; i--) {
          TickAction tickAction = tickActions.get(i);
          int intensity = tickIntensity.get(i);
          if (intensity == 0) {
            builder.append("&7");
          } else if (intensity == 1) {
            builder.append("&a");
          } else if (intensity == 2) {
            builder.append("&e");
          } else if (intensity >= 3) {
            builder.append("&c");
          }
          if (inBlockBreak.get(i)) {
            builder.append(ChatColor.STRIKETHROUGH);
          }
          builder.append(tickAction.repChar());
        }
      } else if (tab == 2) {
        builder.append(" | ");
        Kurtosis.KurtosisMeta kurtosis = (Kurtosis.KurtosisMeta) user.checkMetadata(Kurtosis.KurtosisMeta.class);
        boolean displayMeaning = statsVisible / 20 % 2 == 0 && statsVisible <= 60;

        if (displayMeaning) {
          builder.append("KURTOS");
        } else {
          builder.append(formatDouble(kurtosisOf(kurtosis.attacks), 2));
        }

        builder.append(" ");
        if (displayMeaning) {
          builder.append("SKEWNS");
        } else {
          builder.append(formatDouble(skewnessOf(kurtosis.attacks), 2));
        }

        builder.append(" ");
        if (displayMeaning) {
          builder.append("STDDEV");
        } else {
          builder.append(formatDouble(standardDeviationOf(kurtosis.attacks), 2));
        }
      }

      return ChatColor.translateAlternateColorCodes('&', builder.toString());
    }
  }

  private static double kurtosisOf(Collection<? extends Number> input) {
    double sum = 0;
    int amount = 0;
    for (Number number : input) {
      sum += number.doubleValue();
      ++amount;
    }
    if (amount < 3.0) {
      return 0.0;
    }
    double d2 = amount * (amount + 1.0) / ((amount - 1.0) * (amount - 2.0) * (amount - 3.0));
    double d3 = 3.0 * pow(amount - 1.0, 2.0) / ((amount - 2.0) * (amount - 3.0));
    double average = sum / amount;
    double s2 = 0.0;
    double s4 = 0.0;
    for (Number number : input) {
      s2 += pow(average - number.doubleValue(), 2);
      s4 += pow(average - number.doubleValue(), 4);
    }
    return d2 * (s4 / pow(s2 / sum, 2)) - d3;
  }

  private static double skewnessOf(Collection<? extends Number> sd) {
    int amount = sd.size();
    if (amount == 0) {
      return 0;
    }
    double total = 0;
    List<Double> numbersAsDoubles = new ArrayList<>();
    for (Number number : sd) {
      double numberAsDouble = number.doubleValue();
      total += numberAsDouble;
      numbersAsDoubles.add(numberAsDouble);
    }
    numbersAsDoubles.sort(Double::compareTo);
    double mean = total / amount;
    double median = numbersAsDoubles.get((amount % 2 != 0 ? amount : amount - 1) / 2);
    return 3 * (mean - median) / standardDeviationOf(numbersAsDoubles);
  }

  private static double standardDeviationOf(Collection<? extends Number> sd) {
    double sum = 0, newSum = 0;
    for (Number v : sd) {
      sum = sum + v.doubleValue();
    }
    double mean = sum / sd.size();
    for (Number v : sd) {
      newSum = newSum + (v.doubleValue() - mean) * (v.doubleValue() - mean);
    }
    return Math.sqrt(newSum / sd.size());
  }

  public enum TickAction {
    NOTHING(' '),
    CLICK('C'),
    ATTACK('A'),
    PLACE('P'),
    ;
    private final char representation;

    TickAction(char representation) {
      this.representation = representation;
    }

    public char repChar() {
      return representation;
    }
  }
}
