package org.unicitylabs.sdk.serializer.cbor.predicate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.IOException;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.embedded.MaskedPredicate;
import org.unicitylabs.sdk.predicate.embedded.UnmaskedPredicate;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;


public class MaskedPredicateCbor {

  private MaskedPredicateCbor() {
  }

  public static class Deserializer extends
      JsonDeserializer<MaskedPredicate> {

    @Override
    public MaskedPredicate deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, MaskedPredicate.class, "Expected array value");
      }
      p.nextToken();

      TokenId tokenId = p.readValueAs(TokenId.class);
      TokenType tokenType = p.readValueAs(TokenType.class);
      byte[] publicKey = p.readValueAs(byte[].class);
      String signingAlgorithm = p.readValueAs(String.class);
      HashAlgorithm hashAlgorithm = p.readValueAs(HashAlgorithm.class);
      byte[] nonce = p.readValueAs(byte[].class);

      if (p.nextToken() != JsonToken.END_ARRAY) {
        throw MismatchedInputException.from(p, MaskedPredicate.class, "Expected end of array");
      }

      return new MaskedPredicate(
          tokenId,
          tokenType,
          publicKey,
          signingAlgorithm,
          hashAlgorithm,
          nonce
      );
    }
  }
}
