package com.unicity.sdk.serializer.json.token;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.smt.path.MerkleTreePathStepBranch;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.fungible.CoinId;
import com.unicity.sdk.token.fungible.TokenCoinData;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class TokenCoinDataJson {
    private TokenCoinDataJson() {}


    public static class Serializer extends JsonSerializer<TokenCoinData> {
        public Serializer() {}

        @Override
        public void serialize(TokenCoinData value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }

            gen.writeStartArray();
            for (Map.Entry<CoinId, BigInteger> entry : value.getCoins().entrySet()) {
                gen.writeStartArray();
                gen.writePOJO(entry.getKey().getBytes());
                gen.writeString(entry.getValue().toString());
                gen.writeEndArray();
            }
            gen.writeEndArray();
        }
    }

    public static class Deserializer extends JsonDeserializer<TokenCoinData> {
        public Deserializer() {}

        @Override
        public TokenCoinData deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            if (!p.isExpectedStartArrayToken()) {
                throw MismatchedInputException.from(p, TokenCoinData.class, "Expected array value");
            }

            Map<CoinId, BigInteger> result = new HashMap<CoinId, BigInteger>();
            while (p.nextToken() != JsonToken.END_ARRAY) {
                if (p.currentToken() != JsonToken.START_ARRAY) {
                    throw MismatchedInputException.from(p, TokenCoinData.class, "Expected array of coin data");
                }

                if (p.nextToken() != JsonToken.VALUE_STRING) {
                    throw MismatchedInputException.from(p, TokenCoinData.class, "Expected bytes of coin id");
                }
                CoinId id = new CoinId(p.readValueAs(byte[].class));
                if (p.nextToken() != JsonToken.VALUE_STRING) {
                    throw MismatchedInputException.from(p, TokenCoinData.class, "Expected value as string");
                }
                BigInteger value = new BigInteger(p.getValueAsString());
                if (p.nextToken() != JsonToken.END_ARRAY) {
                    throw MismatchedInputException.from(p, TokenCoinData.class, "Expected end of coin data array");
                }
                if (result.containsKey(id)) {
                    throw MismatchedInputException.from(p, TokenCoinData.class,
                        "Duplicate coin id: " + id);
                }

                result.put(id, value);
            }

            return TokenCoinData.create(result);
        }
    }
}

