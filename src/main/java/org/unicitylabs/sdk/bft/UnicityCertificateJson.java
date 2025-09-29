package org.unicitylabs.sdk.bft;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

/**
 * Unicity certificate serializer and deserializer implementation.
 */
public class UnicityCertificateJson {

  private UnicityCertificateJson() {
  }

  /**
   * Unicity certificate serializer.
   */
  public static class Serializer extends StdSerializer<UnicityCertificate> {

    /**
     * Create serializer.
     */
    public Serializer() {
      super(UnicityCertificate.class);
    }

    /**
     * Serialize unicity certificate.
     *
     * @param value       unicity certificate
     * @param gen         json generator
     * @param serializers serializer provider
     * @throws IOException on serialization failure
     */
    @Override
    public void serialize(UnicityCertificate value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      gen.writeObject(value.toCbor());
    }
  }

  /**
   * Unicity certificate deserializer.
   */
  public static class Deserializer extends StdDeserializer<UnicityCertificate> {

    /**
     * Create deserializer.
     */
    public Deserializer() {
      super(UnicityCertificate.class);
    }

    /**
     * Deserialize unicity certificate.
     *
     * @param p   json parser
     * @param ctx deserialization context
     * @return unicity certificate
     * @throws IOException on deserialization failure
     */
    @Override
    public UnicityCertificate deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      return UnicityCertificate.fromCbor(p.readValueAs(byte[].class));
    }
  }
}

