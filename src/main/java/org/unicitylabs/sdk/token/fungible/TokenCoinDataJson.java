package org.unicitylabs.sdk.token.fungible;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Token coin data json serializer and deserializer.
 */
public class TokenCoinDataJson {

  private TokenCoinDataJson() {
  }

  /**
   * Token coin data serializer.
   */
  public static class Serializer extends StdSerializer<TokenCoinData> {

    /**
     * Create token coin data serializer.
     */
    public Serializer() {
      super(TokenCoinData.class);
    }

    /**
     * Serialize token coin data.
     *
     * @param value       token coin data.
     * @param gen         json generator.
     * @param serializers serializer provider.
     * @throws IOException on serialization failure
     */
    @Override
    public void serialize(TokenCoinData value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      gen.writeStartArray();
      for (Map.Entry<CoinId, BigInteger> entry : value.getCoins().entrySet()) {
        gen.writeStartArray();
        gen.writeObject(HexConverter.encode(entry.getKey().getBytes()));
        gen.writeObject(entry.getValue().toString());
        gen.writeEndArray();
      }
      gen.writeEndArray();
    }
  }

  /**
   * Token coin data deserializer.
   */
  public static class Deserializer extends StdDeserializer<TokenCoinData> {

    /**
     * Create token coin data deserializer.
     */
    public Deserializer() {
      super(TokenCoinData.class);
    }

    /**
     * Deserialize token coin data.
     *
     * @param p   Parser used for reading JSON content
     * @param ctx Context that can be used to access information about this deserialization
     *            activity.
     * @return token coin data
     * @throws IOException on deserialization failure
     */
    @Override
    public TokenCoinData deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      List<String[]> data = ctx.readValue(p,
          ctx.getTypeFactory().constructCollectionType(List.class,
              ctx.getTypeFactory().constructArrayType(String.class)));

      LinkedHashMap<CoinId, BigInteger> coins = new LinkedHashMap<>();
      for (String[] entry : data) {
        if (entry.length != 2) {
          throw MismatchedInputException.from(
              p,
              TokenCoinData.class,
              "Each entry must be an array of two elements: [coinId, amount]"
          );
        }
        try {
          CoinId coinId = new CoinId(HexConverter.decode(entry[0]));
          if (coins.containsKey(coinId)) {
            throw new IOException("Duplicate CoinId: " + coinId);
          }

          coins.put(coinId, new BigInteger(entry[1]));
        } catch (Exception e) {
          throw MismatchedInputException.from(p, "Invalid coin data", e);
        }
      }

      return new TokenCoinData(coins);
    }
  }
}