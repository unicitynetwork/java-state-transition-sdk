package org.unicitylabs.sdk.serializer.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.unicitylabs.sdk.api.NetworkId;

import java.io.IOException;

/**
 * {@link NetworkId} serializer and deserializer implementation that maps to/from a numeric identifier.
 */
public class NetworkIdJson {

  private NetworkIdJson() {
  }

  /**
   * Network id serializer.
   */
  public static class Serializer extends StdSerializer<NetworkId> {

    /**
     * Create serializer.
     */
    public Serializer() {
      super(NetworkId.class);
    }

    /**
     * Serialize network id.
     *
     * @param value       network id
     * @param gen         json generator
     * @param serializers serializer provider
     * @throws IOException on serialization failure
     */
    @Override
    public void serialize(NetworkId value, JsonGenerator gen, SerializerProvider serializers)
            throws IOException {
      gen.writeNumber(value.getId());
    }
  }

  /**
   * Network id deserializer.
   */
  public static class Deserializer extends StdDeserializer<NetworkId> {

    /**
     * Create deserializer.
     */
    public Deserializer() {
      super(NetworkId.class);
    }

    /**
     * Deserialize network id.
     *
     * @param p   Parser used for reading JSON content
     * @param ctx Context that can be used to access information about this deserialization activity.
     * @return network id
     * @throws IOException on deserialization failure
     */
    @Override
    public NetworkId deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      int id = p.getIntValue();
      if (id < 1 || id > 0xFFFF) {
        throw new IllegalArgumentException(
                "Network identifier out of allowed 16-bit unsigned range: " + id + ".");
      }
      return NetworkId.fromId((short) id);
    }
  }
}
