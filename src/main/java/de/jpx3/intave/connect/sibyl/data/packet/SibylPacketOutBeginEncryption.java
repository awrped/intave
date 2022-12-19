package de.jpx3.intave.connect.sibyl.data.packet;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.security.PublicKey;
import java.util.Base64;

public final class SibylPacketOutBeginEncryption extends SibylPacket {
  private byte[] publicKey; // 1024-bit RSA public key
  private byte[] verifyToken; // 4 byte random token

  public SibylPacketOutBeginEncryption(PublicKey publicKey, byte[] verifyToken) {
    super("begin-encryption");
    this.publicKey = publicKey.getEncoded();
    this.verifyToken = verifyToken;
  }

  @Override
  public void buildFrom(JsonElement element) {
    JsonObject object = element.getAsJsonObject();
    publicKey = Base64.getDecoder().decode(object.get("publicKey").getAsString());
    verifyToken = Base64.getDecoder().decode(object.get("verifyToken").getAsString());
  }

  @Override
  public JsonElement asJsonElement() {
    JsonObject object = new JsonObject();
    object.addProperty("publicKey", Base64.getEncoder().encodeToString(publicKey));
    object.addProperty("verifyToken", Base64.getEncoder().encodeToString(verifyToken));
    return object;
  }
}
