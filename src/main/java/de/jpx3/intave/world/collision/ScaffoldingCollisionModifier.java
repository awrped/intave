package de.jpx3.intave.world.collision;

import de.jpx3.intave.tools.wrapper.WrappedAxisAlignedBB;
import de.jpx3.intave.user.User;
import org.bukkit.Material;

import java.util.Collections;
import java.util.List;

public final class ScaffoldingCollisionModifier extends CollisionModifier {
  @Override
  public List<WrappedAxisAlignedBB> modify(User user, WrappedAxisAlignedBB userBox, int posX, int posY, int posZ, List<WrappedAxisAlignedBB> boxes) {
//    boxes = new ArrayList<>(boxes);
//    this.c > (double)var1.getY() + var0.c(EnumAxis.Y) - 9.999999747378752E-6D
    boolean disableHitbox = userBox.minY <= posY + 1 - 0.000009999999747378752;
    if (disableHitbox) {
      return Collections.emptyList();
    }
//    boxes.removeIf(axisAlignedBB -> );
    return boxes;
  }

  @Override
  public boolean matches(Material material) {
    String name = material.name();
    return name.contains("SCAFFOLDING");
  }
}
