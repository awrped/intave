package de.jpx3.intave.user.storage;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

import java.util.Arrays;

public class LatencyStorage implements Storage {
  public int buckets;
  public long[] latencyBuckets = new long[buckets];
  public int backtrackVL;
  public long lastUpdate;

  @Override
  public void writeTo(ByteArrayDataOutput output) {
    output.writeInt(buckets);
    for (long latency : latencyBuckets) {
      output.writeLong(latency);
    }
    output.writeInt(backtrackVL);
    output.writeLong(lastUpdate);
  }

  @Override
  public void readFrom(ByteArrayDataInput input) {
    buckets = input.readInt();
    latencyBuckets = new long[buckets];
    for (int i = 0; i < buckets; i++) {
      latencyBuckets[i] = input.readLong();
    }
    backtrackVL = input.readInt();
    if (System.currentTimeMillis() - lastUpdate > 1_000 * 60 * 60) {
      Arrays.fill(latencyBuckets, 0);
      backtrackVL = 0;
    }
  }

  @Override
  public int id() {
    return 11;
  }

  @Override
  public int version() {
    return 1;
  }
}
