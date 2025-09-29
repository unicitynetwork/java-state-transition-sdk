package org.unicitylabs.sdk.token;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

/**
 * Token type serializer and deserializer implementation.
 */
public class TokenTypeJson {

  private TokenTypeJson() {
  }

  /**
   * Token type serializer.
   */
  public static class Serializer extends StdSerializer<TokenType> {

    /**
     * Create token type serializer.
     */
    public Serializer() {
      super(TokenType.class);
    }

    /**
     * Serialize token type.
     *
     * @param value       token type
     * @param gen         json generator
     * @param serializers serializer provider
     * @throws IOException on serialization failure
     */
    @Override
    public void serialize(TokenType value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeObject(value.getBytes());
    }
  }

  /**
   * Token type deserializer.
   */
  public static class Deserializer extends StdDeserializer<TokenType> {

    /**
     * Create token type deserializer.
     */
    public Deserializer() {
      super(TokenType.class);
    }

    /**
     * Deserialize token type.
     *
     * @param p   Parser used for reading JSON content
     * @param ctx Context that can be used to access information about this deserialization
     *            activity.
     * @return token type
     * @throws IOException on deserialization failure
     */
    @Override
    public TokenType deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      return new TokenType(p.readValueAs(byte[].class));
    }
  }
}

