package de.jpx3.intave.module.linker.packet;

import de.jpx3.intave.user.User;

public interface PlayerPacketEventSubscriber extends PacketEventSubscriber {
  PacketEventSubscriber packetSubscriberFor(User user);
}
