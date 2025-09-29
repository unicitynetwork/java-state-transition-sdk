package org.unicitylabs.sdk.hash;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

/**
 * Data hash serializer and deserializer implementation.
 */
public class DataHashJson {

  private DataHashJson() {
  }

  /**
   * Data hash serializer.
   */
  public static class Serializer extends StdSerializer<DataHash> {

    /**
     * Create serializer.
     */
    public Serializer() {
      super(DataHash.class);
    }

    /**
     * Serialize data hash.
     *
     * @param value       data hash
     * @param gen         json generator
     * @param serializers serializer provider
     * @throws IOException on serialization failure
     */
    @Override
    public void serialize(DataHash value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeObject(value.getImprint());
    }
  }

  /**
   * Data hash deserializer.
   */
  public static class Deserializer extends StdDeserializer<DataHash> {

    /**
     * Create deserializer.
     */
    public Deserializer() {
      super(DataHash.class);
    }

    /**
     * Deserialize data hash.
     *
     * @param p   Parser used for reading JSON content
     * @param ctx Context that can be used to access information about this deserialization
     *            activity.
     * @return data hash
     * @throws IOException on deserialization failure
     */
    @Override
    public DataHash deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      return DataHash.fromImprint(p.readValueAs(byte[].class));
    }
  }
}

