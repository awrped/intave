package de.jpx3.intave.reflect.hitbox.typeaccess;

import de.jpx3.intave.adapter.MinecraftVersions;
import de.jpx3.intave.logging.IntaveLogger;
import de.jpx3.intave.reflect.hitbox.HitBoxBoundaries;

import java.util.HashMap;
import java.util.Map;

public final class DualEntityTypeAccess {
  private final static Map<Integer, HitBoxBoundaries> entityHitBoxMap = new HashMap<>();
  private final static Map<Integer, String> entityNameMap = new HashMap<>();
  private final static boolean NATIVE_RESOLVE = MinecraftVersions.VER1_14_0.atOrAbove();

  static final int ENTITY_ID_LOOKUP = 200;

  public static void setup() {
    if (NATIVE_RESOLVE) {
      for (int id = 0; id < ENTITY_ID_LOOKUP; id++) {
        entityHitBoxMap.put(id, EntityTypeResolverNew.resolveBoundariesOf(id));
        entityNameMap.put(id, EntityTypeResolverNew.resolveNameOf(id));
      }
    } else {
      try {
        EntityTypeResolverLegacy.pollTo(
          entityHitBoxMap,
          entityNameMap
        );
      } catch (Exception | Error throwable) {
        IntaveLogger.logger().info("Failed to perform ETRL setup procedure");
      }
    }
  }

  public static EntityTypeData resolveFromId(int entityTypeId) {
    return new EntityTypeData(nameFromID(entityTypeId), boundariesFromId(entityTypeId), entityTypeId);
  }

  public static HitBoxBoundaries boundariesFromId(int id) {
    return entityHitBoxMap.get(id);
  }

  public static String nameFromID(int id) {
    return entityNameMap.get(id);
  }
}