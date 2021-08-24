package de.jpx3.intave.event.feedback;

import java.util.concurrent.atomic.AtomicLong;

public final class PendingCountingFeedbackTracker implements FeedbackTracker {
  private final AtomicLong counter = new AtomicLong();

  @Override
  public void sent(Request<?> request) {
    counter.incrementAndGet();
  }

  @Override
  public void received(Request<?> request) {
    counter.decrementAndGet();
  }

  public long pending() {
    return counter.get();
  }
}
