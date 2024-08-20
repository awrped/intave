package de.jpx3.intave.module.feedback;

public final class FeedbackOptions {
  public static int SELF_SYNCHRONIZATION = 1;
//  @Deprecated
  public static int APPEND_ON_OVERFLOW = 2;
//  @Deprecated
  public static int APPEND = 4;

  public static int TRACER_ENTITY_IS_NEAR_IN_COMBAT = 1 << 16;
  public static int TRACER_ENTITY_IS_NEAR = 1 << 17;
  public static int TRACER_ENTITY_IS_FAR = 1 << 18;

  public static int TRACER_ENTITY_MOVED_CLOSER = 1 << 19;
  public static int TRACER_ENTITY_MOVED_FARTHER = 1 << 20;

  public static boolean matches(int option, int options) {
    return (options & option) != 0;
  }

  public static int onlyTracerOptions(int options) {
    return options & (TRACER_ENTITY_IS_NEAR_IN_COMBAT | TRACER_ENTITY_IS_NEAR | TRACER_ENTITY_IS_FAR | TRACER_ENTITY_MOVED_CLOSER | TRACER_ENTITY_MOVED_FARTHER);
  }
}
