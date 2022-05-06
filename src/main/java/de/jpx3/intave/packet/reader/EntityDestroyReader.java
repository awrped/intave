package de.jpx3.intave.packet.reader;

import de.jpx3.intave.adapter.MinecraftVersions;

import java.util.List;
import java.util.function.Consumer;

public final class EntityDestroyReader extends AbstractPacketReader {
  private final boolean INT_LIST_ENTITY_DESTROY = MinecraftVersions.VER1_17_1.atOrAbove();
  private final boolean SINGLE_INT_ENTITY_DESTROY = !INT_LIST_ENTITY_DESTROY && MinecraftVersions.VER1_17_0.atOrAbove();
  private final boolean INT_ARRAY_ENTITY_DESTROY = !SINGLE_INT_ENTITY_DESTROY;

  public void readEntities(Consumer<Integer> subscriber) {
    if (INT_LIST_ENTITY_DESTROY) {
      List<Integer> entityIDs = packet().getIntLists().read(0);
      entityIDs.forEach(subscriber);
    } else if (INT_ARRAY_ENTITY_DESTROY) {
      int[] entityIDs = packet().getIntegerArrays().read(0);
      for (int entityID : entityIDs) {
        subscriber.accept(entityID);
      }
    } else {
      subscriber.accept(packet().getIntegers().read(0));
    }
  }
}
