package org.unicitylabs.sdk.signing;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

/**
 * Signature serializer and deserializer implementation.
 */
public class SignatureJson {

  private SignatureJson() {
  }

  /**
   * Signature serializer.
   */
  public static class Serializer extends StdSerializer<Signature> {

    /**
     * Create serializer.
     */
    public Serializer() {
      super(Signature.class);
    }

    /**
     * Serialize signature.
     *
     * @param value       signature
     * @param gen         json generator
     * @param serializers serializer provider
     * @throws IOException on serialization failure
     */
    @Override
    public void serialize(Signature value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      gen.writeObject(value.encode());
    }
  }

  /**
   * Signature deserializer.
   */
  public static class Deserializer extends StdDeserializer<Signature> {

    /**
     * Create deserializer.
     */
    public Deserializer() {
      super(Signature.class);
    }

    /**
     * Deserialize signature.
     *
     * @param p   Parser used for reading JSON content
     * @param ctx Context that can be used to access information about this deserialization
     *            activity.
     * @return signature
     * @throws IOException on deserialization failure
     */
    @Override
    public Signature deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      return Signature.decode(p.readValueAs(byte[].class));
    }
  }
}

