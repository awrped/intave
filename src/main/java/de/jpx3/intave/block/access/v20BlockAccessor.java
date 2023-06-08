package de.jpx3.intave.block.access;

import com.comphenix.protocol.wrappers.BlockPosition;
import de.jpx3.intave.adapter.MinecraftVersions;
import de.jpx3.intave.block.variant.BlockVariantRegister;
import de.jpx3.intave.klass.Lookup;
import de.jpx3.intave.klass.rewrite.PatchyAutoTranslation;
import de.jpx3.intave.klass.rewrite.PatchyTranslateParameters;
import de.jpx3.intave.library.asm.Type;
import de.jpx3.intave.user.User;
import de.jpx3.intave.user.UserRepository;
import de.jpx3.intave.world.WorldHeight;
import net.minecraft.server.level.ChunkProviderServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.IBlockAccess;
import net.minecraft.world.level.block.state.IBlockData;
import net.minecraft.world.level.chunk.LightChunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_20_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.util.CraftMagicNumbers;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

@PatchyAutoTranslation
public final class v20BlockAccessor implements BlockAccessor {
  private static final boolean INVENTORY_VIA_GETTER = MinecraftVersions.VER1_17_0.atOrAbove();

  static {
    checkVersion();
  }

  @PatchyAutoTranslation
  public static void checkVersion() {
    try {
      ChunkProviderServer.class.getMethod("c", int.class, int.class);
    } catch (NoSuchMethodException exception) {
      throw new IllegalStateException("Please update your minecraft version", exception);
    }
  }

  @Override
  @PatchyAutoTranslation
  public Material typeOf(Block block) {
    int blockY = block.getY();
    if (blockY < WorldHeight.LOWER_WORLD_LIMIT || blockY > WorldHeight.UPPER_WORLD_LIMIT) {
      return Material.AIR;
    }
    WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
    IBlockAccess blockAccess = findChunk(worldServer.getChunkProvider(), block.getX() >> 4, block.getZ() >> 4);
    if (blockAccess == null) {
      return Material.AIR;
    }
    IBlockData blockData = blockAccess.getType(positionOfBlock(block));
    net.minecraft.world.level.block.Block nmsBlock = blockData.getBlock();
    return CraftMagicNumbers.getMaterial(nmsBlock);
  }

  @Override
  @PatchyAutoTranslation
  public int variantIndexOf(Block block) {
    int blockY = block.getY();
    if (blockY < WorldHeight.LOWER_WORLD_LIMIT || blockY > WorldHeight.UPPER_WORLD_LIMIT) {
      return 0;
    }
    Material type = typeOf(block);
    IBlockData blockData = (IBlockData) nativeVariantOf(block);
    int variantIndex = BlockVariantRegister.variantIndexOf(type, blockData);
    return Math.max(variantIndex, 0);
  }

  @Override
  @PatchyAutoTranslation
  public Object nativeVariantOf(Block block) {
    int blockY = block.getY();
    if (blockY < WorldHeight.LOWER_WORLD_LIMIT || blockY > WorldHeight.UPPER_WORLD_LIMIT) {
      return net.minecraft.world.level.block.Blocks.a.getBlockData();
    }
    WorldServer worldServer = ((CraftWorld) block.getWorld()).getHandle();
    IBlockAccess blockAccess = findChunk(worldServer.getChunkProvider(), block.getX() >> 4, block.getZ() >> 4);
    if (blockAccess == null) {
      return net.minecraft.world.level.block.Blocks.a.getBlockData();
    }
    return blockAccess.getType(positionOfBlock(block));
  }

  @Override
  @PatchyAutoTranslation
  public Object nativeVariantBy(int blockId) {
    return net.minecraft.world.level.block.Block.getByCombinedId(blockId).getBlock();
  }

  @Override
  @PatchyAutoTranslation
  public float blockDamage(World world, Player player, ItemStack itemInHand, BlockPosition nativeBlockPosition) {
    WorldServer worldServer = ((CraftWorld) world).getHandle();
    IBlockAccess blockAccess = findChunk(worldServer.getChunkProvider(), nativeBlockPosition.getX() >> 4, nativeBlockPosition.getZ() >> 4);
    if (blockAccess == null) {
      return 0.0f;
    }
    net.minecraft.core.BlockPosition blockPosition = positionOfNative(nativeBlockPosition);
    IBlockData blockData = blockAccess.getType(blockPosition);
    EntityPlayer entityPlayer = player == null ? null : ((CraftPlayer) player).getHandle();
    return blockData.getBlock().getDamage(blockData, entityPlayer, blockAccess, blockPosition);
  }

  @Override
  @PatchyAutoTranslation
  public boolean replacementPlace(World world, Player player, BlockPosition nativeBlockPosition) {
    WorldServer worldServer = ((CraftWorld) world).getHandle();
    IBlockAccess blockAccess = findChunk(worldServer.getChunkProvider(), nativeBlockPosition.getX() >> 4, nativeBlockPosition.getZ() >> 4);
    if (blockAccess == null) {
      return false;
    }
    User user = UserRepository.userOf(player);
    int heldSlot = user.meta().inventory().handSlot();
    IBlockData blockData = blockAccess.getType(positionOfNative(nativeBlockPosition));
    Object heldItem;
    if (INVENTORY_VIA_GETTER) {
      heldItem = ((org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer) player).getHandle().getInventory().getItem(heldSlot).getItem();
    } else {
      heldItem = ((CraftPlayer) player).getHandle().getInventory().getItem(heldSlot).getItem();
    }
    Item targetItem = blockData.getBlock().getItem();

    Method method = Lookup.serverMethod("BlockData", "isReplaceable", boolean.class);
    boolean replaceable = false;
    try {
      replaceable = (boolean) method.invoke(blockData);
    } catch (IllegalAccessException | InvocationTargetException ignored) {
      ignored.printStackTrace();
    }

    return replaceable && !Objects.equals(targetItem, heldItem);
  }

  @PatchyAutoTranslation
  @PatchyTranslateParameters
  private net.minecraft.core.BlockPosition positionOfBlock(Block block) {
    return new net.minecraft.core.BlockPosition(block.getX(), block.getY(), block.getZ());
  }

  @PatchyAutoTranslation
  @PatchyTranslateParameters
  private net.minecraft.core.BlockPosition positionOfNative(BlockPosition blockPosition) {
    return new net.minecraft.core.BlockPosition(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ());
  }

  private LightChunk findChunk(ChunkProviderServer server, int x, int z) {
    Class<?> chunk = Lookup.serverClass("LightChunk");
    Method providerMethod = Lookup.serverMethod("ChunkProviderServer", "c", new Type[]{Type.INT_TYPE, Type.INT_TYPE}, Type.getType(chunk));
    try {
      return (LightChunk) providerMethod.invoke(server, x, z);
    } catch (IllegalAccessException | InvocationTargetException ignored) {
    }
    return null;
  }
}
