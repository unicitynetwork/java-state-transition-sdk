package org.unicitylabs.sdk.serializer.cbor.token.fungible;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.unicitylabs.sdk.token.fungible.CoinId;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.util.BigIntegerConverter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

public class TokenCoinDataCbor {

  private TokenCoinDataCbor() {
  }


  public static class Serializer extends JsonSerializer<TokenCoinData> {

    public Serializer() {
    }

    @Override
    public void serialize(TokenCoinData value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      Map<CoinId, BigInteger> coins = value.getCoins();
      gen.writeStartArray(value, coins.size());
      for (Map.Entry<CoinId, BigInteger> entry : coins.entrySet()) {
        gen.writeStartArray(entry, 2);
        gen.writeObject(entry.getKey().getBytes());
        gen.writeObject(BigIntegerConverter.encode(entry.getValue()));
        gen.writeEndArray();
      }
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<TokenCoinData> {

    public Deserializer() {
    }

    @Override
    public TokenCoinData deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, TokenCoinData.class, "Expected array value");
      }

      Map<CoinId, BigInteger> coins = new LinkedHashMap<>();
      while (p.nextToken() != JsonToken.END_ARRAY) {
        if (!p.isExpectedStartArrayToken()) {
          throw MismatchedInputException.from(p, TokenCoinData.class,
              "Expected array value for coin entry");
        }

        CoinId coinId = new CoinId(p.readValueAs(byte[].class));
        if (coins.containsKey(coinId)) {
          throw MismatchedInputException.from(p, TokenCoinData.class,
              "Duplicate coin ID in coin data: " + coinId);
        }

        BigInteger amount = BigIntegerConverter.decode(p.readValueAs(byte[].class));
        coins.put(coinId, amount);
      }

      return new TokenCoinData(coins);
    }
  }
}

