package de.jpx3.intave.packet.reader;

import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import com.google.common.collect.Lists;
import de.jpx3.intave.klass.Lookup;
import de.jpx3.intave.packet.converter.BlockPositionConverter;

import java.util.List;

public final class SingleBlockChangeReader extends AbstractPacketReader implements BlockChanges {
  @Override
  public List<BlockPosition> blockPositions() {
    BlockPosition blockPosition = packet().getModifier()
      .withType(Lookup.serverClass("BlockPosition"), BlockPositionConverter.threadConverter())
      .read(0);
    return Lists.newArrayList(blockPosition);
//    return Lists.newArrayList(packet.getBlockPositionModifier().readSafely(0));
  }

  @Override
  public List<WrappedBlockData> blockDataList() {
    return Lists.newArrayList(packet().getBlockData().read(0));
  }
}
