package de.jpx3.intave.fakeplayer.randomaction;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import de.jpx3.intave.fakeplayer.FakePlayer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

public abstract class RandomAction {
  public final static ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
  protected final Player parentPlayer;
  protected final FakePlayer fakePlayer;
  private final ActionType type;
  private final Probability probability;
  private int loop = 0;

  public RandomAction(
    Probability probability,
    ActionType type,
    Player player,
    FakePlayer fakePlayer
  ) {
    this.probability = probability;
    this.type = type;
    this.parentPlayer = player;
    this.fakePlayer = fakePlayer;
  }

  public static void findAndProcessAction(
    Collection<RandomAction> randomActions,
    ActionType type
  ) {
    for (RandomAction randomAction : randomActions) {
      if (randomAction.type == type) {
        randomAction.performAction();
      }
    }
  }

  public final void mayProcess() {
    if (++loop % this.probability.probability() == 0) {
      performAction();
    } else {
      unsafeAction();
    }
  }

  public void unsafeAction() {
  }

  protected abstract void performAction();

  public enum Probability {
    HIGH(5, 50),
    MEDIUM(40, 80),
    LOW(400, 700);

    private final int minProbability;
    private final int maxProbability;

    Probability(int minProbability, int maxProbability) {
      this.minProbability = minProbability;
      this.maxProbability = maxProbability;
    }

    private int probability() {
      return ThreadLocalRandom.current().nextInt(this.minProbability, this.maxProbability);
    }
  }
}