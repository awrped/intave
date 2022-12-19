package de.jpx3.intave.packet.reader;

import org.jetbrains.annotations.NotNull;

/**
 * Class generated using IntelliJ IDEA
 * Created by Richard Strunk 2022
 */

public interface EntityIterable extends PacketReader, Iterable<Integer> {
  @NotNull
  @Override
  SubstitutionIterator<Integer> iterator();
}
