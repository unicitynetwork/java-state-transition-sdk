package org.unicitylabs.sdk.serializer.cbor.predicate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.predicate.BurnPredicate;
import org.unicitylabs.sdk.predicate.PredicateType;
import java.io.IOException;

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
      gen.writeObject(value.getType());
      gen.writeObject(value.getNonce());
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

      String type = p.readValueAs(String.class);
      if (!PredicateType.BURN.name().equals(type)) {
        throw MismatchedInputException.from(p, BurnPredicate.class,
            "Expected predicate type to be " + PredicateType.BURN);
      }

      return new BurnPredicate(p.readValueAs(byte[].class), p.readValueAs(DataHash.class));
    }
  }
}
