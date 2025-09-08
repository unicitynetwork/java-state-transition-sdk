package org.unicitylabs.sdk.serializer.cbor.predicate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.PredicateType;
import org.unicitylabs.sdk.predicate.UnmaskedPredicate;
import java.io.IOException;

public class UnmaskedPredicateCbor {
  private UnmaskedPredicateCbor() {
  }

  public static class Serializer extends JsonSerializer<UnmaskedPredicate> {

    @Override
    public void serialize(UnmaskedPredicate value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 5);
      gen.writeObject(value.getType());
      gen.writeObject(value.getPublicKey());
      gen.writeObject(value.getSigningAlgorithm());
      gen.writeObject(value.getHashAlgorithm().getValue());
      gen.writeObject(value.getNonce());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends
      JsonDeserializer<UnmaskedPredicate> {

    @Override
    public UnmaskedPredicate deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, UnmaskedPredicate.class, "Expected array value");
      }

      String type = p.readValueAs(String.class);
      if (!PredicateType.UNMASKED.name().equals(type)) {
        throw MismatchedInputException.from(p, UnmaskedPredicate.class,
            "Expected predicate type to be " + PredicateType.UNMASKED);
      }

      return new UnmaskedPredicate(
          p.readValueAs(byte[].class),
          p.readValueAs(String.class),
          p.readValueAs(HashAlgorithm.class),
          p.readValueAs(byte[].class)
      );
    }
  }
}
