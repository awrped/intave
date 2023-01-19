package de.jpx3.intave.check.combat;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.annotate.Relocate;
import de.jpx3.intave.check.CheckViolationLevelDecrementer;
import de.jpx3.intave.check.MetaCheck;
import de.jpx3.intave.diagnostic.message.DebugBroadcast;
import de.jpx3.intave.diagnostic.message.MessageCategory;
import de.jpx3.intave.diagnostic.message.MessageSeverity;
import de.jpx3.intave.math.MathHelper;
import de.jpx3.intave.module.Modules;
import de.jpx3.intave.module.linker.packet.ListenerPriority;
import de.jpx3.intave.module.linker.packet.PacketSubscription;
import de.jpx3.intave.module.mitigate.AttackNerfStrategy;
import de.jpx3.intave.module.tracker.entity.Entity;
import de.jpx3.intave.module.tracker.entity.EntityTracker;
import de.jpx3.intave.module.violation.Violation;
import de.jpx3.intave.module.violation.ViolationContext;
import de.jpx3.intave.share.Position;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.meta.*;
import de.jpx3.intave.world.raytrace.Raytrace;
import de.jpx3.intave.world.raytrace.Raytracing;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static de.jpx3.intave.module.linker.packet.PacketId.Client.*;

@Relocate
public final class AttackRaytrace extends MetaCheck<AttackRaytrace.AttackRaytraceMeta> {
  private static final char[] VOCALS = "aeiou".toCharArray();
  private final IntavePlugin plugin;
  private final CheckViolationLevelDecrementer hitboxDecrementer, reachDecrementer;
  private final double VL_DECREMENT_PER_ATTACK = 0.125;
  private static final int MAX_ALLOWED_PENDING_ATTACKS = 5;

//  private final static boolean HAS_MYTHIC_MOBS = Bukkit.getPluginManager().isPluginEnabled("MythicMobs");

  public AttackRaytrace(IntavePlugin plugin) {
    super("AttackRaytrace", "attackraytrace", AttackRaytraceMeta.class);
    this.plugin = plugin;
    this.hitboxDecrementer = new CheckViolationLevelDecrementer(this, "applicable-thresholds.hitbox", VL_DECREMENT_PER_ATTACK * 0.5);
    this.reachDecrementer = new CheckViolationLevelDecrementer(this, "applicable-thresholds.reach", VL_DECREMENT_PER_ATTACK * 2);
  }

  @PacketSubscription(
      priority = ListenerPriority.LOW,
      packetsIn = {
          USE_ENTITY
      }
  )
  public void receiveUseEntityPacket(PacketEvent event) {
    Player player = event.getPlayer();
    User user = userOf(player);
    AttackRaytraceMeta attackRaytraceMeta = metaOf(user);
    PacketContainer packet = event.getPacket();
    EnumWrappers.EntityUseAction action = packet.getEntityUseActions().readSafely(0);
    if (action == null) {
      action = packet.getEnumEntityUseActions().read(0).getAction();
    }
    // Only process attacks, interactions should not be checked
    if (action == EnumWrappers.EntityUseAction.ATTACK) {
      List<Attack> pendingAttacks = attackRaytraceMeta.pendingAttacks;
      int entityId = packet.getIntegers().read(0);
      PacketContainer clone = packet.deepClone();
      Entity entity = EntityTracker.entityByIdentifier(user, entityId);
      // Allow attacks on invalid entity states
      if (entity == null || entity instanceof Entity.Destroyed) {
        return;
      }
      Attack attack = new Attack(clone, entityId, false);
      // Only add attack to queue if queue size is small enough
      if (pendingAttacks.size() < MAX_ALLOWED_PENDING_ATTACKS) {
        pendingAttacks.add(attack);
      }
    }
  }

  @PacketSubscription(
      priority = ListenerPriority.HIGH,
      packetsIn = {
          FLYING, LOOK, POSITION, POSITION_LOOK
      }
  )
  public void receiveMovementPacket(PacketEvent event) {
    Player player = event.getPlayer();
    User user = userOf(player);
    AttackRaytraceMeta attackRaytraceMeta = metaOf(user);
    AbilityMetadata abilityData = user.meta().abilities();
    MovementMetadata movementData = user.meta().movement();
    ProtocolMetadata clientData = user.meta().protocol();
    List<Attack> pendingAttacks = attackRaytraceMeta.pendingAttacks;
    PacketContainer packet = event.getPacket();
    // Clear attacks if recently teleported
    if (movementData.lastTeleport == 0) {
      pendingAttacks.clear();
    }
    // Apply flying packets (first boolean)
    if (!packet.getBooleans().read(1)) {
      attackRaytraceMeta.flyingPacketCounter++;
    } else {
      attackRaytraceMeta.flyingPacketCounter = 0;
    }
    // Process all pending attacks
    for (Attack pendingAttack : pendingAttacks) {
      float entityHealth = abilityData.unsynchronizedHealth;
      Entity attackedEntity = EntityTracker.entityByIdentifier(user, pendingAttack.entityId);
      // Once again ignore invalid entity states to make sure nothing is processed wrongly
      if (entityHealth <= 0 || attackedEntity == null || attackedEntity instanceof Entity.Destroyed) {
        return;
      }
      boolean entityOutOfSync = (!clientData.flyingPacketStream() && movementData.recentlyEncounteredFlyingPacket(2))
          || !attackedEntity.clientSynchronized;
      // This might seem confusing but this is definitely required! DO NOT TINKER
      if (entityOutOfSync) {
        processAttackRaytraceBruteforceFor(user, attackedEntity, pendingAttack);
      } else {
        processAttackRaytraceFor(user, attackedEntity, pendingAttack, computeExpansionFor(user));
      }
    }
    pendingAttacks.clear();
  }

  /**
   * Processes the reach check 3x for all possible entity and player positions (Interpolation in client is 3 ticks long).
   * Takes the lowest reach calculated as a result of the calculation.
   * <p>
   * This is required when we don't know the exact position of the entity as the player
   * either didn't send flying packets or it's not synchronized yet
   *
   * @param user   The user which attacked
   * @param entity The attacked entity
   * @param attack The current attack
   * @since 14.5.8
   */
  private void processAttackRaytraceBruteforceFor(User user, Entity entity, Attack attack) {
    Raytrace lowestRaytrace = fireRaytraceFor(user, entity, 0.13f);
    double lowestReach = lowestRaytrace.reach();
    Entity cloned = entity.temporaryCopy();
    boolean living = cloned.typeData().isLivingEntity();
    // Calculate raytrace for all pos increments
    while (cloned.position.newPosRotationIncrements > 0 && living) {
      cloned.onUpdate();
      Raytrace raytrace = fireRaytraceFor(user, entity, 0.13f);
      double reach = raytrace.reach();
      // Set reach and raytrace if lower
      if (reach < lowestReach) {
        lowestReach = reach;
        lowestRaytrace = raytrace;
      }
      // Don't do any extra calculations if obsolete
      if (reach <= 3.0) {
        break;
      }
    }
    processResult(user, lowestRaytrace, entity, 0.13f, true);
  }

  /**
   * Processes the reach check for a given user
   *
   * @param user      The user which attacked
   * @param entity    The attacked entity
   * @param attack    The current attack
   * @param expansion The hit-box expansion applied for the player (this differs depending on the client)
   * @since 14.5.8
   */
  private void processAttackRaytraceFor(
      User user,
      Entity entity,
      Attack attack,
      float expansion
  ) {
    Raytrace raytrace = fireRaytraceFor(user, entity, expansion);
    processResult(user, raytrace, entity, expansion, false);
  }

  /**
   * Processes the raytrace result and creates violations from it if calculations exceed legit values
   *
   * @param user      The user to process the raytrace for
   * @param raytrace  The raytrace
   * @param attacked  The attacked entity
   * @param expansion The hitbox expansion used while raytracing
   * @param estimated Whether the raytrace was estimated or not (will not give vl if it is)
   * @since 14.5.8
   */
  private void processResult(
      User user,
      Raytrace raytrace,
      Entity attacked,
      float expansion,
      boolean estimated
  ) {
    Player player = user.player();
    MetadataBundle meta = user.meta();
    String entityName = attacked.entityName();
    double blockReachDistance = Raytracing.reachDistance(meta);
    RaytraceResult result = RaytraceResult.of(raytrace, blockReachDistance);
    int vl = calculateVlFor(user, raytrace, result, attacked, expansion, estimated);
    String estimationSuffix = estimated ? " (estimated)" : "";
    String message, details, thresholdKey, sibyl;
    double reach = 0;
    System.out.println("Server Reach: " + raytrace.reach() + estimationSuffix);
    switch (result) {
      case MISS: {
        message = "attacked " + resolveArticle(entityName) + " " + entityName.toLowerCase() + " out of sight" + estimationSuffix;
        details = "";
        thresholdKey = "applicable-thresholds.hitbox";
        sibyl = player.getName() + "/" + user.protocolVersion() + " missed hit on " + entityName.toLowerCase();
        reach = -1;
        break;
      }
      case REACH: {
        String displayReach = MathHelper.formatDouble(raytrace.reach(), 4);
        message = "attacked " + resolveArticle(entityName) + " " + entityName.toLowerCase() + " from too far away" + estimationSuffix;
        details = displayReach + " blocks";
        thresholdKey = "applicable-thresholds.reach";
        sibyl = player.getName() + "/" + user.protocolVersion() + " attacked " + entityName.toLowerCase() + " from " + displayReach;
        reach = raytrace.reach();
        break;
      }
      default: {
        hitboxDecrementer.decrement(user, VL_DECREMENT_PER_ATTACK);
        reachDecrementer.decrement(user, VL_DECREMENT_PER_ATTACK);
        return;
      }
    }
    DebugBroadcast.broadcast(player, MessageCategory.ATRAFLT, MessageSeverity.HIGH, sibyl, sibyl);
    Violation violation = Violation.builderFor(AttackRaytrace.class)
        .forPlayer(player).withMessage(message).withDetails(details)
        .withCustomThreshold(thresholdKey).withVL(vl)
        .withPlaceholder("reach", MathHelper.formatDouble(reach, 4))
        .build();
    ViolationContext violationContext = Modules.violationProcessor().processViolation(violation);
    // Apply damage cancel after 50 VL
    if (violationContext.violationLevelAfter() > 50 && !estimated) {
      //dmc3
      user.nerf(AttackNerfStrategy.CRITICALS, "3");
      user.nerf(AttackNerfStrategy.BURN_LONGER, "3");
      user.nerf(AttackNerfStrategy.BLOCKING, "3");
    }
  }

  /**
   * Computes violation points for an evaluated {@link Raytrace} which will get applied to a {@link Player}
   *
   * @param user      The user to compute violation points for
   * @param raytrace  The raytrace
   * @param result    The raytrace result
   * @param attacked  The attacked entity
   * @param expansion The hit-box expansion used
   * @param estimated Whether the raytrace was estimated or not
   * @return The computed violation points
   * @since 14.5.8
   */
  private int calculateVlFor(
      User user,
      Raytrace raytrace,
      RaytraceResult result,
      Entity attacked,
      float expansion,
      boolean estimated
  ) {
    AttackRaytraceMeta attackRaytraceMeta = metaOf(user);
    Position targetPosition = raytrace.targetPosition();
    boolean invalidRaytrace = user.meta().movement().isInVehicle()
        || (targetPosition != null && attackRaytraceMeta.lastPosition != null && targetPosition.distance(attackRaytraceMeta.lastPosition) == 0);
    // Do not apply violation points if the raytrace was estimated or invalid
    if (estimated || invalidRaytrace) {
      return 0;
    }
    int vl = result.violationProvider().apply(attacked);
    // Reduce vl if hit-box was enlarged due to flying packets
    if (expansion > 0.1f) {
      vl /= 2f;
    }
    attackRaytraceMeta.lastPosition = raytrace.targetPosition();
    return vl;
  }

  /**
   * Fires an entity raytrace for the given user
   *
   * @param user      The user
   * @param entity    The entity
   * @param expansion The hit-box expansion
   * @return The raytrace result
   * @since 14.5.8
   */
  private Raytrace fireRaytraceFor(
      User user,
      Entity entity,
      float expansion
  ) {
    MetadataBundle meta = user.meta();
    MovementMetadata movementData = meta.movement();
    ProtocolMetadata clientData = meta.protocol();

    boolean requiresAlternativeY = clientData.flyingPacketStream();
    boolean fixedMouseDelay = clientData.protocolVersion() >= 314;
    float yaw = movementData.rotationYaw % 360f;
    float lastYaw = movementData.lastRotationYaw % 360f;

    return Raytracing.doubleMDFBlockConstraintEntityRaytrace(
        user.player(),
        entity, requiresAlternativeY,
        movementData.lastPositionX, movementData.lastPositionY, movementData.lastPositionZ,
        lastYaw,
        yaw, movementData.rotationPitch,
        expansion,
        !fixedMouseDelay
    );
  }

  /**
   * Computes the hit box expansion for the player
   *
   * @param user The user which is used to compute the expansion
   * @return The expansion
   */
  private float computeExpansionFor(User user) {
    MetadataBundle meta = user.meta();
    ProtocolMetadata clientData = meta.protocol();
    AttackRaytraceMeta attackRaytraceMeta = metaOf(user);
    // Process 1.8 and lower
    if (clientData.flyingPacketStream()) {
      return attackRaytraceMeta.flyingPacketCounter > 0 ? 0.13f : 0.1f;
    } else {
      // TODO: LabyMod Support
      return 0f;
    }
  }

  /**
   * Resolves what article to use for a given entity name
   *
   * @param entityName The entity name
   * @return The article
   * @since 14.5.8
   */
  private String resolveArticle(String entityName) {
    char c = entityName.trim().toLowerCase(Locale.ROOT).toCharArray()[0];
    boolean isVocal = false;
    for (char vocal : VOCALS) {
      if (vocal == c) {
        isVocal = true;
        break;
      }
    }
    return isVocal ? "an" : "a";
  }

  /**
   * The custom check meta for the {@link AttackRaytrace} check
   *
   * @since 14.5.8
   */
  public static class AttackRaytraceMeta extends CheckCustomMetadata {
    public int flyingPacketCounter = 0;
    public List<Attack> pendingAttacks = new ArrayList<>();
    public Position lastPosition;
  }

  /**
   * The attack stored to be processed after a movement packet was sent by the client
   *
   * @since 14.5.8
   */
  public static class Attack {
    private final boolean shouldResend;
    private final PacketContainer packet;
    private final int entityId;

    public Attack(PacketContainer packet, int entityId, boolean shouldResend) {
      this.packet = packet;
      this.entityId = entityId;
      this.shouldResend = shouldResend;
    }

    public PacketContainer packet() {
      return packet;
    }

    public int entityId() {
      return entityId;
    }

    public boolean shouldResend() {
      return shouldResend;
    }
  }

  /**
   * Used to evaluate a {@link Raytrace} for applying violation levels to a {@link Player}
   *
   * @author Lennox
   * @since 14.5.8
   */
  public enum RaytraceResult {
    VALID(e -> 0),
    REACH(e -> 20),
    MISS(e -> e.typeData().isLivingEntity() ? 4 : 0);

    private final Function<Entity, Integer> violationProvider;

    RaytraceResult(Function<Entity, Integer> violationProvider) {
      this.violationProvider = violationProvider;
    }

    public Function<Entity, Integer> violationProvider() {
      return violationProvider;
    }

    /**
     * Evaluates a {@link RaytraceResult} based off a given {@link Raytrace} and block reach limit
     *
     * @param raytrace The raytrace
     * @param limit    The reach limit
     * @return The result
     * @since 14.5.8
     */
    public static RaytraceResult of(Raytrace raytrace, double limit) {
      double reach = raytrace.reach();
      if (reach == 10) {
        return MISS;
      } else if (reach > limit) {
        return REACH;
      } else {
        return VALID;
      }
    }
  }
}