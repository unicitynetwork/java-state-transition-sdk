package com.unicity.sdk.serializer.json.token.fungible;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.token.fungible.CoinId;
import com.unicity.sdk.token.fungible.TokenCoinData;
import java.io.IOException;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.Map;

public class TokenCoinDataJson {

  private TokenCoinDataJson() {
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
        gen.writeString(entry.getValue().toString());
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

      Map<CoinId, BigInteger> result = new LinkedHashMap<>();
      while (p.nextToken() != JsonToken.END_ARRAY) {
        if (!p.isExpectedStartArrayToken()) {
          throw MismatchedInputException.from(p, TokenCoinData.class,
              "Expected array of coin data");
        }

        if (p.nextToken() != JsonToken.VALUE_STRING) {
          throw MismatchedInputException.from(p, TokenCoinData.class, "Expected bytes of coin id");
        }
        CoinId id = new CoinId(p.readValueAs(byte[].class));
        if (p.nextToken() != JsonToken.VALUE_STRING) {
          throw MismatchedInputException.from(p, TokenCoinData.class, "Expected value as string");
        }
        BigInteger value = new BigInteger(p.readValueAs(String.class));
        if (p.nextToken() != JsonToken.END_ARRAY) {
          throw MismatchedInputException.from(p, TokenCoinData.class,
              "Expected end of coin data array");
        }
        if (result.containsKey(id)) {
          throw MismatchedInputException.from(p, TokenCoinData.class,
              "Duplicate coin id: " + id);
        }

        result.put(id, value);
      }

      return new TokenCoinData(result);
    }
  }
}

