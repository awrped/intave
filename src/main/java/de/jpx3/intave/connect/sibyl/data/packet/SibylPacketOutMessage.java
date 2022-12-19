package de.jpx3.intave.connect.sibyl.data.packet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class SibylPacketOutMessage extends SibylPacket {
  private int debugId;

  public SibylPacketOutMessage() {
    super("out-message");
  }

  @Override
  public JsonElement asJsonElement() {
    JsonObject object = new JsonObject();
    object.addProperty("id", debugId);
    return object;
  }

  @Override
  public void buildFrom(JsonElement element) {
    JsonObject object = element.getAsJsonObject();
    debugId = object.get("id").getAsInt();
  }

  public int debugId() {
    return debugId;
  }

  public void setDebugId(int debugId) {
    this.debugId = debugId;
  }
}
