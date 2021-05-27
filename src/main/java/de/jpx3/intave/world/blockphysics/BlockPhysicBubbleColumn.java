package de.jpx3.intave.world.blockphysics;

import com.comphenix.protocol.utility.MinecraftVersion;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserMetaClientData;
import de.jpx3.intave.user.UserMetaMovementData;
import de.jpx3.intave.world.fluid.Fluid;
import de.jpx3.intave.world.fluid.FluidTag;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;

import java.util.Collections;
import java.util.List;

public final class BlockPhysicBubbleColumn implements BlockPhysic {
  private Material bubbleColumnBlock;

  @Override
  public void setup(MinecraftVersion serverVersion) {
    bubbleColumnBlock = Material.getMaterial("BUBBLE_COLUMN");
  }

  @Override
  public boolean supportedOnServerVersion() {
    return bubbleColumnBlock != null;
  }

  @Override
  public Vector entityCollidedWithBlock(User user, Location location, Location from, double motionX, double motionY, double motionZ) {
    try {
      UserMetaClientData clientData = user.meta().clientData();
      if (clientData.waterUpdate()) {
        boolean water = Fluid.fluidAt(user, location.getX(), location.getY() + 1, location.getZ()).isIn(FluidTag.WATER);
        Block block = location.getBlock();
        boolean downwards = block.toString().contains("drag=true");
        if (water) {
          return enterBubbleColumn(user, downwards, motionX, motionY, motionZ);
        } else {
          return enterBubbleColumnWithAirAbove(downwards, motionX, motionY, motionZ);
        }
      }
    } catch (Exception | Error e ) {
      e.printStackTrace();
    }
    return null;
  }

  private Vector enterBubbleColumn(User user, boolean downwards, double motionX, double motionY, double motionZ) {
    UserMetaMovementData movementData = user.meta().movementData();
    if (downwards) {
      motionY = Math.max(-0.3D, motionY - 0.03D);
    } else {
      motionY = Math.min(0.7D, motionY + 0.06D);
    }
    movementData.artificialFallDistance = 0;
    return new Vector(motionX, motionY, motionZ);
  }

  private Vector enterBubbleColumnWithAirAbove(boolean downwards, double motionX, double motionY, double motionZ) {
    if (downwards) {
      motionY = Math.max(-0.9D, motionY - 0.03D);
    } else {
      motionY = Math.min(1.8D, motionY + 0.1D);
    }
    return new Vector(motionX, motionY, motionZ);
  }

  @Override
  public List<Material> materials() {
    return Collections.singletonList(bubbleColumnBlock);
  }
}