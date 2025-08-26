package com.unicity.sdk.serializer.cbor.predicate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.predicate.MaskedPredicate;
import com.unicity.sdk.predicate.PredicateType;
import java.io.IOException;

public class MaskedPredicateCbor {

  private MaskedPredicateCbor() {
  }

  public static class Serializer extends JsonSerializer<MaskedPredicate> {

    @Override
    public void serialize(MaskedPredicate value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 5);
      gen.writeObject(value.getType());
      gen.writeObject(value.getPublicKey());
      gen.writeObject(value.getAlgorithm());
      gen.writeObject(value.getHashAlgorithm().getValue());
      gen.writeObject(value.getNonce());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends
      JsonDeserializer<MaskedPredicate> {

    @Override
    public MaskedPredicate deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, MaskedPredicate.class, "Expected array value");
      }

      String type = p.readValueAs(String.class);
      if (!PredicateType.MASKED.name().equals(type)) {
        throw MismatchedInputException.from(p, MaskedPredicate.class,
            "Expected predicate type to be " + PredicateType.MASKED);
      }

      return new MaskedPredicate(
          p.readValueAs(byte[].class),
          p.readValueAs(String.class),
          p.readValueAs(HashAlgorithm.class),
          p.readValueAs(byte[].class)
      );
    }
  }
}
