package de.jpx3.intave.adapter.viaversion;

import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

public final class ViaVersion4Access implements ViaVersionAccess {
  private Object viaVersionTarget;
  private Method getPlayerVersionMethod;

  @Override
  public void setup() {
    try {
      Class<?> apiAcessorClass = Class.forName("com.viaversion.viaversion.api.Via");
      this.viaVersionTarget = apiAcessorClass.getMethod("getAPI").invoke(null);
      this.getPlayerVersionMethod = Class.forName("com.viaversion.viaversion.api.ViaAPI").getMethod("getPlayerVersion", UUID.class);
    } catch (Exception exception) {
      throw new IllegalStateException("Invalid ViaVersion linkage", exception);
    }
  }

  @Override
  public int protocolVersionOf(Player player) {
    try {
      return (int) getPlayerVersionMethod.invoke(viaVersionTarget, player.getUniqueId());
    } catch (Exception exception) {
      throw new IllegalStateException("Unable to resolve player version", exception);
    }
  }

  @Override
  public boolean ignoreBlocking(Player player) {
    return false;
  }

  @Override
  public boolean available() {
    try {
      Class.forName("com.viaversion.viaversion.api.Via");
      return true;
    } catch (ClassNotFoundException exception) {
      return false;
    }
  }
}
