package org.unicitylabs.sdk.token;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

/**
 * Token id serializer and deserializer implementation.
 */
public class TokenIdJson {

  private TokenIdJson() {
  }

  /**
   * Token id serializer.
   */
  public static class Serializer extends StdSerializer<TokenId> {

    /**
     * Create token id serializer.
     */
    public Serializer() {
      super(TokenId.class);
    }


    /**
     * Serialize token id.
     *
     * @param value       token id
     * @param gen         json generator
     * @param serializers serializer provider
     * @throws IOException on serialization failure
     */
    @Override
    public void serialize(TokenId value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      gen.writeObject(value.getBytes());
    }
  }

  /**
   * Token id deserializer.
   */
  public static class Deserializer extends StdDeserializer<TokenId> {

    /**
     * Create token id deserializer.
     */
    public Deserializer() {
      super(TokenId.class);
    }


    /**
     * Deserialize token id.
     *
     * @param p   Parser used for reading JSON content
     * @param ctx Context that can be used to access information about this deserialization
     *            activity.
     * @return token id
     * @throws IOException on deserialization failure
     */
    @Override
    public TokenId deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (p.getCurrentToken() != JsonToken.VALUE_STRING) {
        throw MismatchedInputException.from(
            p,
            TokenId.class,
            "Expected string value"
        );
      }

      try {
        return new TokenId(p.readValueAs(byte[].class));
      } catch (Exception e) {
        throw MismatchedInputException.from(p, TokenId.class, "Expected bytes");
      }
    }
  }
}

