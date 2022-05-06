package de.jpx3.intave.check;

import de.jpx3.intave.user.meta.CheckCustomMetadata;

public abstract class Blueprint<PARENT extends Check, BLUEPRINT_META extends CheckCustomMetadata, CHECK_META extends BLUEPRINT_META> extends MetaCheckPart<PARENT, CHECK_META> {
  public Blueprint(PARENT parentCheck, Class<CHECK_META> metaClass) {
    super(parentCheck, metaClass);
  }
}
