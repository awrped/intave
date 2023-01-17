package de.jpx3.intave.check.combat;

import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import de.jpx3.intave.IntavePlugin;
import de.jpx3.intave.annotate.Relocate;
import de.jpx3.intave.check.CheckViolationLevelDecrementer;
import de.jpx3.intave.check.MetaCheck;
import de.jpx3.intave.module.linker.packet.ListenerPriority;
import de.jpx3.intave.module.linker.packet.PacketSubscription;
import de.jpx3.intave.module.tracker.entity.Entity;
import de.jpx3.intave.module.tracker.entity.EntityTracker;
import de.jpx3.intave.share.Position;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.meta.*;
import de.jpx3.intave.world.raytrace.Raytrace;
import de.jpx3.intave.world.raytrace.Raytracing;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static de.jpx3.intave.module.linker.packet.PacketId.Client.*;

@Relocate
public final class AttackRaytrace extends MetaCheck<AttackRaytrace.AttackRaytraceMeta> {
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
      priority = ListenerPriority.NORMAL,
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
    double lowestReach = fireRaytraceFor(user, entity, 0.13f).reach();
    Entity cloned = entity.temporaryCopy();
    boolean living = cloned.typeData().isLivingEntity();
    // Calculate raytrace for all pos increments
    while (cloned.position.newPosRotationIncrements > 0 && living) {
      cloned.onUpdate();
      double reach = fireRaytraceFor(user, entity, 0.13f).reach();
      // Set reach if lower
      if (reach < lowestReach) {
        lowestReach = reach;
      }
      // Don't do any extra calculations if obsolete
      if (reach <= 3.0) {
        break;
      }
    }

    user.player().sendMessage((lowestReach <= 3 ? ChatColor.AQUA.toString() : ChatColor.RED.toString())
        + lowestReach + " ESTIMATED Blocks Reach (0.13)");
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
  private void processAttackRaytraceFor(User user, Entity entity, Attack attack, float expansion) {
    Raytrace raytrace = fireRaytraceFor(user, entity, expansion);
    user.player().sendMessage((raytrace.reach() <= 3 ? ChatColor.AQUA.toString() : ChatColor.RED.toString())
        + raytrace.reach() + " Blocks Reach (" + expansion + ")");
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
  private Raytrace fireRaytraceFor(User user, Entity entity, float expansion) {
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
   * The custom check meta for the {@link AttackRaytrace} check
   *
   * @since 14.5.8
   */
  public static class AttackRaytraceMeta extends CheckCustomMetadata {
    public int flyingPacketCounter = 0;
    public List<Attack> pendingAttacks = new ArrayList<>();
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
}