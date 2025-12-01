package org.unicitylabs.sdk.token.fungible;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.unicitylabs.sdk.token.TokenId;

/**
 * Token coin json serializer and deserializer.
 */
public class CoinIdJson {

  private CoinIdJson() {
  }

  /**
   * Token coin serializer.
   */
  public static class Serializer extends StdSerializer<CoinId> {

    /**
     * Create token coin serializer.
     */
    public Serializer() {
      super(CoinId.class);
    }

    /**
     * Serialize token coin.
     *
     * @param value       token coin.
     * @param gen         json generator.
     * @param serializers serializer provider.
     * @throws IOException on serialization failure
     */
    @Override
    public void serialize(CoinId value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeObject(value.getBytes());
    }
  }

  /**
   * Token coin deserializer.
   */
  public static class Deserializer extends StdDeserializer<CoinId> {

    /**
     * Create token coin deserializer.
     */
    public Deserializer() {
      super(CoinId.class);
    }

    /**
     * Deserialize token coin.
     *
     * @param p   Parser used for reading JSON content
     * @param ctx Context that can be used to access information about this deserialization
     *            activity.
     * @return token coin data
     * @throws IOException on deserialization failure
     */
    @Override
    public CoinId deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      if (p.getCurrentToken() != JsonToken.VALUE_STRING) {
        throw MismatchedInputException.from(
            p,
            TokenId.class,
            "Expected string value"
        );
      }

      try {
        return new CoinId(p.readValueAs(byte[].class));
      } catch (Exception e) {
        throw MismatchedInputException.from(p, TokenId.class, "Expected bytes");
      }
    }
  }
}