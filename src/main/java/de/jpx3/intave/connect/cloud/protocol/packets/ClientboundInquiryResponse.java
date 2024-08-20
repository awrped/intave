package de.jpx3.intave.connect.cloud.protocol.packets;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import de.jpx3.intave.connect.cloud.protocol.Direction;
import de.jpx3.intave.connect.cloud.protocol.JsonPacket;
import de.jpx3.intave.connect.cloud.protocol.listener.Clientbound;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class ClientboundInquiryResponse extends JsonPacket<Clientbound> {
  private UUID requestId;
  private Map<String, String> response = new LinkedHashMap<>();

  public ClientboundInquiryResponse() {
    super(Direction.CLIENTBOUND, "INQUIRY_RESPONSE", "1");
  }

  public ClientboundInquiryResponse(UUID requestId, Map<String, String> response) {
    this();
    this.requestId = requestId;
    this.response = response;
  }

  @Override
  public void serialize(JsonWriter writer) {
    try {
      writer.beginObject();
      writer.name("requestId").value(requestId.toString());
      writer.name("response");
      writer.beginObject();
      for (Map.Entry<String, String> entry : response.entrySet()) {
        writer.name(entry.getKey()).value(entry.getValue());
      }
      writer.endObject();
      writer.endObject();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void deserialize(JsonReader reader) {
    response = new LinkedHashMap<>();
    try {
      reader.beginObject();
      while (reader.hasNext()) {
        while (reader.peek() == JsonToken.NAME) {
          switch (reader.nextName()) {
            case "requestId":
              requestId = UUID.fromString(reader.nextString());
              break;
            case "response":
              reader.beginObject();
              while (reader.hasNext()) {
                response.put(reader.nextName(), reader.nextString());
              }
              reader.endObject();
              break;
          }
        }
        if (reader.hasNext()) {
          reader.skipValue();
        }
      }
      reader.endObject();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public UUID requestId() {
    return requestId;
  }

  public Map<String, String> response() {
    return response;
  }
}
