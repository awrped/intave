package de.jpx3.intave.check.world.interaction;

import de.jpx3.intave.annotate.KeepEnumInternalNames;
import de.jpx3.intave.check.world.InteractionRaytrace;

import static de.jpx3.intave.check.world.InteractionRaytrace.ResponseType.CANCEL;
import static de.jpx3.intave.check.world.InteractionRaytrace.ResponseType.RAYTRACE_CAST;

@KeepEnumInternalNames
public enum InteractionType {
  BREAK(CANCEL, false),
  START_BREAK(RAYTRACE_CAST, false),
  INTERACT(RAYTRACE_CAST, false),
  EMPTY_INTERACT(CANCEL, false),
  PLACE(RAYTRACE_CAST, false);

  final InteractionRaytrace.ResponseType response;
  final boolean bufferAvailable;

  InteractionType(InteractionRaytrace.ResponseType response, boolean bufferAvailable) {
    this.response = response;
    this.bufferAvailable = bufferAvailable;
  }

  public InteractionRaytrace.ResponseType response() {
    return response;
  }
}
