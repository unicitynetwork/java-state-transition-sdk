package org.unicitylabs.sdk.serializer.cbor.predicate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.IOException;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.predicate.embedded.BurnPredicate;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;


public class BurnPredicateCbor {
  private BurnPredicateCbor() {
  }

  public static class Serializer extends JsonSerializer<BurnPredicate> {

    @Override
    public void serialize(BurnPredicate value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 3);
      gen.writeObject(value.getTokenId());
      gen.writeObject(value.getTokenType());
      gen.writeObject(value.getReason());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends
      JsonDeserializer<BurnPredicate> {

    @Override
    public BurnPredicate deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, BurnPredicate.class, "Expected array value");
      }
      p.nextToken();

      TokenId tokenId = p.readValueAs(TokenId.class);
      TokenType tokenType = p.readValueAs(TokenType.class);
      DataHash reason = p.readValueAs(DataHash.class);

      if (p.nextToken() != JsonToken.END_ARRAY) {
        throw MismatchedInputException.from(p, BurnPredicate.class, "Expected end of array");
      }

      return new BurnPredicate(tokenId, tokenType, reason);
    }
  }
}
