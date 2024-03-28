package de.jpx3.intave.check;

import de.jpx3.intave.user.User;

/**
 * Player-owned check parts allow class-fields to be used as player meta.
 * Use <code>check.appendPlayerCheckPart(MyClass.class);</code> to load it on startup.
 * @param <P> the parent check
 */
public abstract class PlayerCheckPart<P extends Check> extends CheckPart<P> {
  private final User user;

  public PlayerCheckPart(User user, P parentCheck) {
    super(parentCheck);
    this.user = user;
  }

  public User owningUser() {
    return this.user;
  }
}
