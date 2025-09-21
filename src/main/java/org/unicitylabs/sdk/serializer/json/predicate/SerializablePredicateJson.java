package org.unicitylabs.sdk.serializer.json.predicate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.unicitylabs.sdk.predicate.EncodedPredicate;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.predicate.PredicateEngineType;
import java.io.IOException;
import org.unicitylabs.sdk.predicate.SerializablePredicate;
import org.unicitylabs.sdk.predicate.embedded.EmbeddedPredicateType;

public class SerializablePredicateJson {
  private SerializablePredicateJson() {
  }

  public static class Serializer extends JsonSerializer<SerializablePredicate> {

    @Override
    public void serialize(SerializablePredicate value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 3);
      gen.writeObject(value.getEngine().ordinal());
      gen.writeObject(value.encode());
      gen.writeObject(value.encodeParameters());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<SerializablePredicate> {

    @Override
    public SerializablePredicate deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, Predicate.class, "Expected array value");
      }

      p.nextToken();
      PredicateEngineType engine = p.readValueAs(PredicateEngineType.class);
      byte[] code = p.readValueAs(byte[].class);
      byte[] parameters = p.readValueAs(byte[].class);

      if (p.nextToken() != JsonToken.END_ARRAY) {
        throw MismatchedInputException.from(p, Predicate.class, "Expected end of array");
      }

      return new EncodedPredicate(engine, code, parameters);
    }
  }
}