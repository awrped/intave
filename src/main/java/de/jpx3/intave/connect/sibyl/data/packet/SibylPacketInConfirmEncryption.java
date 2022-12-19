package de.jpx3.intave.connect.sibyl.data.packet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Base64;

public final class SibylPacketInConfirmEncryption extends SibylPacket {
  private byte[] secretKeyEncrypted;
  private byte[] verifyTokenEncrypted;

  public SibylPacketInConfirmEncryption() {
    super("confirm-encryption");
  }

  @Override
  public void buildFrom(JsonElement element) {
    JsonObject object = element.getAsJsonObject();
    secretKeyEncrypted = Base64.getDecoder().decode(object.get("sharedSecret").getAsString());
    verifyTokenEncrypted = Base64.getDecoder().decode(object.get("confirmation").getAsString());
  }

  @Override
  public JsonElement asJsonElement() {
    JsonObject object = new JsonObject();
    object.addProperty("sharedSecret", Base64.getEncoder().encodeToString(secretKeyEncrypted));
    object.addProperty("confirmation", Base64.getEncoder().encodeToString(verifyTokenEncrypted));
    return object;
  }

  public byte[] encryptedSharedSecret() {
    return secretKeyEncrypted;
  }

  public byte[] encryptedVerifyToken() {
    return verifyTokenEncrypted;
  }
}
