package de.jpx3.intave.tools.client;

import de.jpx3.intave.tools.wrapper.WrappedAxisAlignedBB;
import de.jpx3.intave.tools.wrapper.WrappedMathHelper;
import de.jpx3.intave.user.*;
import de.jpx3.intave.world.blockaccess.BukkitBlockAccess;
import de.jpx3.intave.world.collision.Collision;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.material.Directional;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Openable;

import java.util.List;

public final class PlayerMovementHelper {
  public static double jumpMotionFor(Player player) {
    User user = UserRepository.userOf(player);
    UserMetaPotionData potionData = user.meta().potionData();
    double motionY = 0.42f;
    if (potionData.potionEffectJumpDuration > 0) {
      int jumpAmplifier = potionData.potionEffectJumpAmplifier();
      motionY += (float) ((jumpAmplifier + 1) * 0.1);
    }
    return motionY;
  }

  public static float resolveSlipperiness(User user, Location location) {
    Material type = BukkitBlockAccess.cacheAppliedTypeAccess(user, location);
    float blockSlipperiness;
    switch (type) {
      case PACKED_ICE:
      case ICE: {
        blockSlipperiness = 0.98f;
        break;
      }
      case SLIME_BLOCK: {
        blockSlipperiness = 0.8f;
        break;
      }
      default: {
        blockSlipperiness = 0.6f;
      }
    }
    return blockSlipperiness * 0.91f;
  }

  public static float resolveFriction(User user, double positionX, double positionY, double positionZ) {
    UserMetaMovementData movementData = user.meta().movementData();
    World world = user.player().getWorld();
    float speed;
    if (movementData.lastOnGround) {
      Location location = new Location(
        world,
        WrappedMathHelper.floor(positionX),
        WrappedMathHelper.floor(positionY - movementData.frictionPosSubtraction()),
        WrappedMathHelper.floor(positionZ)
      );
      float slipperiness = PlayerMovementHelper.resolveSlipperiness(user, location);
      float var4 = 0.16277136f / (slipperiness * slipperiness * slipperiness);
      speed = movementData.aiMoveSpeed() * var4;
    } else {
      speed = movementData.jumpMovementFactor();
    }
    return speed;
  }

  public static boolean isOffsetPositionInLiquid(
    Player player,
    WrappedAxisAlignedBB entityBoundingBox,
    double x, double y, double z
  ) {
    return isLiquidPresentInAABB(player, entityBoundingBox.offset(x, y, z));
  }

  private static boolean isLiquidPresentInAABB(Player player, WrappedAxisAlignedBB boundingBox) {
    List<WrappedAxisAlignedBB> collisionBoxes = Collision.resolve(player, boundingBox);
    return collisionBoxes.isEmpty() && !isAnyLiquid(player.getWorld(), boundingBox);
  }

  public static boolean isAnyLiquid(World world, WrappedAxisAlignedBB boundingBox) {
    int minX = WrappedMathHelper.floor(boundingBox.minX);
    int minY = WrappedMathHelper.floor(boundingBox.minY);
    int minZ = WrappedMathHelper.floor(boundingBox.minZ);
    int maxX = WrappedMathHelper.floor(boundingBox.maxX);
    int maxY = WrappedMathHelper.floor(boundingBox.maxY);
    int maxZ = WrappedMathHelper.floor(boundingBox.maxZ);
    for (int x = minX; x <= maxX; ++x) {
      for (int y = minY; y <= maxY; ++y) {
        for (int z = minZ; z <= maxZ; ++z) {
          Material material = BukkitBlockAccess.blockAccess(world, x, y, z).getType();
          if (MaterialLogic.isLiquid(material)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static boolean isAllLiquid(World world, WrappedAxisAlignedBB boundingBox) {
    int minX = WrappedMathHelper.floor(boundingBox.minX);
    int minY = WrappedMathHelper.floor(boundingBox.minY);
    int minZ = WrappedMathHelper.floor(boundingBox.minZ);
    int maxX = WrappedMathHelper.floor(boundingBox.maxX);
    int maxY = WrappedMathHelper.floor(boundingBox.maxY);
    int maxZ = WrappedMathHelper.floor(boundingBox.maxZ);
    for (int x = minX; x <= maxX; ++x) {
      for (int y = minY; y <= maxY; ++y) {
        for (int z = minZ; z <= maxZ; ++z) {
          Material material = BukkitBlockAccess.blockAccess(world, x, y, z).getType();
          if (!MaterialLogic.isLiquid(material)) {
            return false;
          }
        }
      }
    }
    return true;
  }

  public static boolean isLavaInBB(World world, WrappedAxisAlignedBB boundingBox) {
    int minX = WrappedMathHelper.floor(boundingBox.minX);
    int minY = WrappedMathHelper.floor(boundingBox.minY);
    int minZ = WrappedMathHelper.floor(boundingBox.minZ);
    int maxX = WrappedMathHelper.floor(boundingBox.maxX + 1.0D);
    int maxY = WrappedMathHelper.floor(boundingBox.maxY + 1.0D);
    int maxZ = WrappedMathHelper.floor(boundingBox.maxZ + 1.0D);
    for (int x = minX; x < maxX; ++x) {
      for (int y = minY; y < maxY; ++y) {
        for (int z = minZ; z < maxZ; ++z) {
          if (MaterialLogic.isLava(BukkitBlockAccess.blockAccess(world, x, y, z).getType())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  public static boolean isOnLadder(User user, double positionX, double positionY, double positionZ) {
    Player player = user.player();
    UserMetaClientData clientData = user.meta().clientData();
    Material type = BukkitBlockAccess.cacheAppliedTypeAccess(
      user, player.getWorld(),
      WrappedMathHelper.floor(positionX),
      WrappedMathHelper.floor(positionY),
      WrappedMathHelper.floor(positionZ)
    );
    if (clientData.protocolVersion() > 47 && type.name().contains("TRAP_DOOR")) {
      return true;
    }
    return type == Material.LADDER || type == Material.VINE;
  }

  private static boolean canGoThroughTrapDoorOnLadder(Block block) {
    Location location = block.getLocation();
    BlockState blockState = block.getState();
    MaterialData data = blockState.getData();
    if (data instanceof Openable && (((Openable) data).isOpen())) {
      Directional directional = (Directional) blockState.getData();
      Location downLocation = location.clone().add(0.0, -1.0, 0.0);
      Block downBlock = BukkitBlockAccess.blockAccess(downLocation);
      if (!(downBlock instanceof Directional)) {
        return false;
      }
      Directional downBlockDirectional = (Directional) downBlock.getState().getData();
      return downBlock.getType() == Material.LADDER && directional.getFacing() == downBlockDirectional.getFacing();
    }
    return false;
  }
}