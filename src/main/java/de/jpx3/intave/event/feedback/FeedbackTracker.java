package de.jpx3.intave.event.feedback;

public interface FeedbackTracker {
  void sent(Request<?> request);

  void received(Request<?> request);
}
