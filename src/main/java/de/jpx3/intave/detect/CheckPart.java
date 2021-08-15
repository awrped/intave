package de.jpx3.intave.detect;

import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserRepository;
import org.bukkit.entity.Player;

/**
 * A {@link CheckPart} is a single detection algorithm in a detection cluster
 * specifically bound to and held by a parent {@link Check}.
 * By holding a parent {@link Check} reference, it constrains
 * itself to only be bound to one parent check.
 *
 * A check part can by itself state whether it wants linkage (see {@link CheckPart#enabled()} for reference.
 * @param <P> parent check type
 */
public abstract class CheckPart<P extends Check> implements EventProcessor {
  private final P parentCheck;

  public CheckPart(P parentCheck) {
    this.parentCheck = parentCheck;
  }

  /**
   * Performs a {@link User} lookup of a corresponding {@link Player}.
   * @param player the player search
   * @return a blank or corresponding user
   */
  protected final User userOf(Player player) {
    return UserRepository.userOf(player);
  }

  /**
   * Retrieves the parent check
   * @return the parent check
   */
  public final P parentCheck() {
    return parentCheck;
  }

  /**
   * States whether the {@link CheckPart} is enabled and therefore subject to linkage.
   * By default, the method just follows the the parent check.
   * @return whether the check part is enabled and therefore subject to linkage
   */
  public boolean enabled() {
    return parentCheck.enabled();
  }
}