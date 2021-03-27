package de.jpx3.intave.fakeplayer.randomaction.actions;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.google.common.collect.Lists;
import de.jpx3.intave.fakeplayer.FakePlayer;
import de.jpx3.intave.fakeplayer.FakePlayerMetaDataHelper;
import de.jpx3.intave.fakeplayer.randomaction.ActionType;
import de.jpx3.intave.fakeplayer.randomaction.RandomAction;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static de.jpx3.intave.fakeplayer.FakePlayer.SPAWN_HEALTH_STATE;

public final class HurtAnimationAction extends RandomAction {
  private final static byte DAMAGE_ANIMATION = 1;
  private final static double VELOCITY_CONVERT_FACTOR = 8000.0D;
  private final static List<Double> horizontalVelocities = Lists.newArrayList();
  private final static List<Double> verticalVelocities = Lists.newArrayList();
  private float currentHealthState = SPAWN_HEALTH_STATE;
  private long lastNaturalHealthUpdate = System.currentTimeMillis();

  public HurtAnimationAction(Player player, FakePlayer fakePlayer) {
    super(Probability.MEDIUM, ActionType.HURT_ANIMATION, player, fakePlayer);
  }

  @Override
  protected void performAction() {
    sendHurtAnimation();
    sendEntityVelocity();
  }

  @Override
  public void unsafeAction() {
    long time = System.currentTimeMillis();
    long timePassed = time - lastNaturalHealthUpdate;
    long expectedTime = currentHealthState < 5 ? 1_000 : 3_000;
    if (timePassed > expectedTime && currentHealthState < 20) {
      sendHealthUpdate(currentHealthState);
      lastNaturalHealthUpdate = time;
    }
  }

  private void sendHurtAnimation() {
    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ANIMATION);
    packet.getIntegers().writeSafely(0, this.fakePlayer.fakePlayerEntityId());
    packet.getModifier().writeSafely(1, DAMAGE_ANIMATION);
    try {
      protocolManager.sendServerPacket(this.parentPlayer, packet);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
    sendHealthUpdate(Math.max(1, currentHealthState - ThreadLocalRandom.current().nextInt(1, 4)));
  }

  private void sendEntityVelocity() {
    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ENTITY_VELOCITY);
    packet.getIntegers().writeSafely(0, this.fakePlayer.fakePlayerEntityId());
    double motionX = randomHorizontalVelocity();
    double motionY = randomVerticalVelocity();
    double motionZ = randomHorizontalVelocity();
    packet.getIntegers().writeSafely(1, (int) (motionX * VELOCITY_CONVERT_FACTOR));
    packet.getIntegers().writeSafely(2, (int) (motionY * VELOCITY_CONVERT_FACTOR));
    packet.getIntegers().writeSafely(3, (int) (motionZ * VELOCITY_CONVERT_FACTOR));
    try {
      protocolManager.sendServerPacket(this.parentPlayer, packet);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }

  private void sendHealthUpdate(float health) {
    FakePlayerMetaDataHelper.updateHealthFor(parentPlayer, fakePlayer, SPAWN_HEALTH_STATE);
    if (health != this.currentHealthState) {
      FakePlayerMetaDataHelper.updateHealthFor(parentPlayer, fakePlayer, health);
    }
    this.currentHealthState = health;
  }

  private double randomHorizontalVelocity() {
    if (horizontalVelocities.size() <= 2) {
      return ThreadLocalRandom.current().nextDouble(-0.5, 0.5);
    }
    int position = ThreadLocalRandom.current().nextInt(0, horizontalVelocities.size() - 1);
    return horizontalVelocities.get(position);
  }

  private double randomVerticalVelocity() {
    if (verticalVelocities.size() <= 2) {
      return ThreadLocalRandom.current().nextDouble(0.0, 0.4);
    }
    int position = ThreadLocalRandom.current().nextInt(0, verticalVelocities.size() - 1);
    return verticalVelocities.get(position);
  }

  public static class VelocityListener extends PacketAdapter {
    public VelocityListener(Plugin plugin) {
      super(plugin, PacketType.Play.Server.ENTITY_VELOCITY);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
      PacketContainer packet = event.getPacket();
      double motionX = packet.getIntegers().readSafely(1) / VELOCITY_CONVERT_FACTOR;
      double motionY = packet.getIntegers().readSafely(2) / VELOCITY_CONVERT_FACTOR;
      double motionZ = packet.getIntegers().readSafely(3) / VELOCITY_CONVERT_FACTOR;
      registerHorizontalVelocity(motionX);
      registerVerticalVelocity(motionY);
      registerHorizontalVelocity(motionZ);
    }

    private void registerHorizontalVelocity(double velocity) {
      if (!horizontalVelocities.contains(velocity)) {
        horizontalVelocities.add(velocity);
      }
    }

    private void registerVerticalVelocity(double velocity) {
      if (!verticalVelocities.contains(velocity)) {
        verticalVelocities.add(velocity);
      }
    }
  }
}