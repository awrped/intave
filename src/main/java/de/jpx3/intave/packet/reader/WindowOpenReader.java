package de.jpx3.intave.packet.reader;

import de.jpx3.intave.adapter.MinecraftVersions;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public final class WindowOpenReader extends AbstractPacketReader implements EntityIterable {
  public int containerId() {
    return packet().getIntegers().read(0);
  }

  public int slots() {
    if (!MinecraftVersions.VER1_14_0.atOrAbove()) {
      return packet().getIntegers().read(1);
    } else {
      int menuType = packet().getIntegers().read(0);
      switch (menuType) {
        case 0:
          return 9;
        case 1:
          return 18;
        case 2:
          return 27;
        case 3:
          return 36;
        case 4:
          return 45;
        case 5:
          return 54;
        default:
          return 27;
      }
    }
  }

  public Optional<Integer> optionalEntityId() {
    Integer read = packet().getIntegers().read(2);
    if (read == null) {
      return Optional.empty();
    } else if (read == 0) {
      return Optional.empty();
    }
    return Optional.of(read);
  }

  @Override
  public @NotNull SubstitutionIterator<Integer> iterator() {
    Optional<Integer> integer = optionalEntityId();
    if (!integer.isPresent()) {
      return new SubstitutionIterator<Integer>() {
        @Override
        public void set(Integer integer) {
        }

        @Override
        public boolean hasNext() {
          return false;
        }

        @Override
        public Integer next() {
          throw new UnsupportedOperationException();
        }
      };
    } else {
      return new SubstitutionIterator<Integer>() {
        boolean hasNext = false;

        @Override
        public void set(Integer integer) {
          packet().getIntegers().write(2, integer);
        }

        @Override
        public boolean hasNext() {
          return hasNext;
        }

        @Override
        public Integer next() {
          hasNext = false;
          return integer.get();
        }
      };
    }
  }
}
