package de.jpx3.intave.connect.cloud.protocol.pipeline;

import de.jpx3.intave.connect.cloud.Session;
import de.jpx3.intave.connect.cloud.protocol.Packet;
import de.jpx3.intave.connect.cloud.protocol.PacketRegistry;
import de.jpx3.intave.connect.cloud.protocol.listener.Clientbound;
import de.jpx3.intave.connect.cloud.protocol.packets.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import static de.jpx3.intave.connect.cloud.protocol.Direction.CLIENTBOUND;

public final class StandardClientRetriever extends ChannelInboundHandlerAdapter implements Clientbound {
  private final Session session;

  public StandardClientRetriever(Session session) {
    this.session = session;
  }

  @Override
  public void channelRead(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
    if (o instanceof Packet) {
      Packet<?> packet = (Packet<?>) o;
      if (packet.direction() == CLIENTBOUND) {
        System.out.println("[Intave/Cloud] Received " + packet.name());
        onSelect(packet);
      }
    }
  }

  @Override
  public void onCloseConnection(ClientboundDisconnectPacket packet) {
    System.out.println("Connection closed: " + packet.reason());
    session.close();
  }

  @Override
  public void onClientHello(ClientboundHelloPacket packet) {
    throw new RuntimeException("Unexpected packet " + packet.name());
  }

  @Override
  public void onKeepAlive(ClientboundKeepAlivePacket packet) {
    // do nothing
  }

  @Override
  public void onSetTrustfactor(ClientboundSetTrustfactorPacket packet) {
    session.serveTrustfactorRequest(packet.id(), packet.trustFactor());
  }

  @Override
  public void onDownloadStorage(ClientboundDownloadStoragePacket packet) {
    session.serveStorageRequest(packet.id(), packet.data());
  }
}
