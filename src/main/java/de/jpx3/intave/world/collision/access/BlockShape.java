package de.jpx3.intave.world.collision.access;

import de.jpx3.intave.tools.AccessHelper;
import de.jpx3.intave.tools.wrapper.WrappedAxisAlignedBB;
import org.bukkit.Material;

import java.util.List;

public final class BlockShape {
  private final List<WrappedAxisAlignedBB> boxes;
  private final Material type;
  private final int data;
  private final long creation = AccessHelper.now();

  public BlockShape(List<WrappedAxisAlignedBB> boxes, Material type, int data) {
    this.boxes = boxes;
    this.type = type;
    this.data = data;
  }

  public List<WrappedAxisAlignedBB> boundingBoxes() {
    return boxes;
  }

  public Material type() {
    return type;
  }

  public int data() {
    return data;
  }

  public boolean expired() {
    return AccessHelper.now() - creation > 10000;
  }
}
