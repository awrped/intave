package de.jpx3.intave.fakeplayer;

import com.comphenix.protocol.wrappers.WrappedGameProfile;
import com.google.common.base.Preconditions;
import de.jpx3.intave.fakeplayer.movement.types.Movement;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class FakePlayerBuilder {
  private Player parentPlayer = null;
  private Movement movement = null;
  private UUID uuid = null;
  private int timeout = 10_000;
  private int entityID = -1;
  private String name;
  private String tabListPrefix;
  private String prefix;

  private FakePlayerBuilder() {
  }

  public static FakePlayerBuilder createBuilder() {
    return new FakePlayerBuilder();
  }

  public FakePlayerBuilder setParentPlayer(Player player) {
    this.parentPlayer = player;
    return this;
  }

  public FakePlayerBuilder setUUID(UUID uuid) {
    this.uuid = uuid;
    return this;
  }

  public FakePlayerBuilder setEntityID(int entityID) {
    this.entityID = entityID;
    return this;
  }

  public FakePlayerBuilder setName(String name) {
    this.name = name;
    return this;
  }

  public FakePlayerBuilder setTabListPrefix(String tabListPrefix) {
    this.tabListPrefix = tabListPrefix;
    return this;
  }

  public FakePlayerBuilder setPrefix(String prefix) {
    this.prefix = prefix;
    return this;
  }

  public FakePlayerBuilder setMovement(Movement movement) {
    this.movement = movement;
    return this;
  }

  public FakePlayerBuilder setTimeout(int timeout) {
    this.timeout = timeout;
    return this;
  }

  public FakePlayer build() {
    Preconditions.checkNotNull(this.parentPlayer);
    Preconditions.checkNotNull(this.uuid);
    Preconditions.checkState(this.entityID >= 0, "EntityId can not be negative!");
    Preconditions.checkNotNull(this.name);
    Preconditions.checkNotNull(this.movement);
    int nameLength = this.name.length();
    Preconditions.checkState(nameLength <= 16, "Name is too long (" + nameLength + ")");
    return new FakePlayer(
      this.movement,
      this.parentPlayer,
      new WrappedGameProfile(this.uuid, this.name),
      this.tabListPrefix == null ? this.name : this.tabListPrefix,
      this.prefix == null ? this.name : this.prefix,
      this.entityID,
      this.timeout
    );
  }
}
