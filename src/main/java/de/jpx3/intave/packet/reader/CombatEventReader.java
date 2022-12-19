package de.jpx3.intave.packet.reader;

import org.jetbrains.annotations.NotNull;

public final class CombatEventReader extends AbstractPacketReader implements EntityIterable{
  public int firstEntityId() {
    return packet().getIntegers().read(0);
  }

  public int secondEntityId() {
    return packet().getIntegers().read(1);
  }

  private int slot = 0;

  @Override
  public @NotNull SubstitutionIterator<Integer> iterator() {
    slot = 0;
    return STATIC_ITERATOR;
  }

  private final SubstitutionIterator<Integer> STATIC_ITERATOR = new SubstitutionIterator<Integer>() {
    @Override
    public void set(Integer integer) {
      if (slot == 1) {
        packet().getIntegers().write(0, integer);
      } else if (slot == 2) {
        packet().getIntegers().write(1, integer);
      }
    }

    @Override
    public boolean hasNext() {
      return slot < 2;
    }

    @Override
    public Integer next() {
      return packet().getIntegers().read(slot++);
    }
  };
}
