package de.jpx3.intave.user.storage;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;

public class LatencyStorage implements Storage {
  public float mean, variance, amount;
  public int backtrackVL;

  @Override
  public void writeTo(ByteArrayDataOutput output) {
    output.writeFloat(mean);
    output.writeFloat(variance);
    output.writeFloat(amount);
    output.writeInt(backtrackVL);
  }

  @Override
  public void readFrom(ByteArrayDataInput input) {
    mean = input.readFloat();
    variance = input.readFloat();
    amount = input.readFloat();
    backtrackVL = input.readInt();
  }

  @Override
  public int id() {
    return 11;
  }

  @Override
  public int version() {
    return 0;
  }
}
