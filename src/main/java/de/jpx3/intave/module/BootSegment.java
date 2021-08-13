package de.jpx3.intave.module;

public enum BootSegment {
  @Deprecated
  STAGE_1, // IntavePlugin clinit
  STAGE_2, // IntavePlugin init
  STAGE_3, // onLoad
  STAGE_4, // onEnable start
  STAGE_5, // onEnable (protocollib guaranteed)
  STAGE_6, // onEnable (alpha stage)
  STAGE_7, // onEnable (beta stage)
  STAGE_8, // onEnable (gamma stage)
  STAGE_9, // onEnable (theta stage)
  STAGE_10, // onEnable complete
  STAGE_11, // first tick
}
