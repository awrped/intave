package de.jpx3.intave.fakeplayer.randomaction.actions;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import de.jpx3.intave.fakeplayer.FakePlayer;
import de.jpx3.intave.fakeplayer.randomaction.ActionType;
import de.jpx3.intave.fakeplayer.randomaction.RandomAction;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public final class SwingAnimationAction extends RandomAction {
  private final static byte SWING_ANIMATION = 0;

  public SwingAnimationAction(Player player, FakePlayer fakePlayer) {
    super(Probability.HIGH, ActionType.SWING_ANIMATION, player, fakePlayer);
  }

  @Override
  protected void performAction() {
    PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.ANIMATION);
    packet.getIntegers().writeSafely(0, this.fakePlayer.fakePlayerEntityId());
    packet.getBytes().writeSafely(0, SWING_ANIMATION);
    try {
      protocolManager.sendServerPacket(this.parentPlayer, packet);
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }
}
