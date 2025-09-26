package org.unicitylabs.sdk.serializer.cbor.predicate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.unicitylabs.sdk.predicate.embedded.DefaultPredicate;

public class DefaultPredicateCbor {
  private DefaultPredicateCbor() {
  }

  public static class Serializer extends JsonSerializer<DefaultPredicate> {
    @Override
    public void serialize(DefaultPredicate value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 6);
      gen.writeObject(value.getTokenId());
      gen.writeObject(value.getTokenType());
      gen.writeObject(value.getPublicKey());
      gen.writeObject(value.getSigningAlgorithm());
      gen.writeObject(value.getHashAlgorithm());
      gen.writeObject(value.getNonce());
      gen.writeEndArray();
    }
  }
}
