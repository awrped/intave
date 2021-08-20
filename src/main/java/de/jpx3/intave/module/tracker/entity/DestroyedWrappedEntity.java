package de.jpx3.intave.module.tracker.entity;

import de.jpx3.intave.reflect.hitbox.HitBoxBoundaries;
import de.jpx3.intave.reflect.hitbox.typeaccess.EntityTypeData;

public final class DestroyedWrappedEntity extends WrappedEntity {
  public DestroyedWrappedEntity() {
    super(0, new EntityTypeData("destroyed", HitBoxBoundaries.zero(),-1, false), false);
  }

  @Override
  void onLivingUpdate() {
  }
}
