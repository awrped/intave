package de.jpx3.intave.packet.reader;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.MovingObjectPositionBlock;
import de.jpx3.intave.adapter.MinecraftVersions;
import de.jpx3.intave.klass.Lookup;
import de.jpx3.intave.packet.converter.BlockPositionConverter;

public class BlockPositionReader extends AbstractPacketReader {
  private final boolean MODERN_RESOLVE = MinecraftVersions.VER1_14_0.atOrAbove();

  public BlockPosition blockPosition() {
    if (MODERN_RESOLVE) {
      MovingObjectPositionBlock movingObjectPositionBlock = packet().getMovingBlockPositions().readSafely(0);
      return movingObjectPositionBlock == null ? null : movingObjectPositionBlock.getBlockPosition();
    } else {
      return packet().getModifier()
        .withType(Lookup.serverClass("BlockPosition"), BlockPositionConverter.threadConverter())
        .readSafely(0);
    }
  }
}
