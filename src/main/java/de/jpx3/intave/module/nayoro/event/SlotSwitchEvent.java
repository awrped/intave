package de.jpx3.intave.module.nayoro.event;

import de.jpx3.intave.math.MathHelper;
import de.jpx3.intave.module.nayoro.Environment;
import de.jpx3.intave.module.nayoro.event.sink.EventSink;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public final class SlotSwitchEvent extends Event {
  private int slot;
  private String material;
  private int amount;

  public SlotSwitchEvent() {
  }

  public SlotSwitchEvent(int slot, String material, int amount) {
    this.slot = slot;
    this.material = material;
    this.amount = amount;
  }

  @Override
  public void serialize(Environment environment, DataOutput out) throws IOException {
    out.writeInt(slot);
    out.writeUTF(material);
    out.writeInt(amount);
  }

  @Override
  public void deserialize(Environment environment, DataInput in) throws IOException {
    slot = in.readInt();
    material = in.readUTF();
    amount = in.readInt();
  }

  @Override
  public void accept(EventSink sink) {
    sink.visit(this);
  }

  public static SlotSwitchEvent create(int slot, String material, int amount) {
    return new SlotSwitchEvent(slot, material, MathHelper.minmax(0, amount, 64));
  }
}
