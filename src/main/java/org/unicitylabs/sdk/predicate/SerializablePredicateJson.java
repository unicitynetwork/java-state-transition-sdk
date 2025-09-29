package org.unicitylabs.sdk.predicate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;

public class SerializablePredicateJson {

  private SerializablePredicateJson() {
  }

  public static class Serializer extends StdSerializer<SerializablePredicate> {

    public Serializer() {
      super(SerializablePredicate.class);
    }

    @Override
    public void serialize(SerializablePredicate value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeObject(
          CborSerializer.encodeArray(
              CborSerializer.encodeUnsignedInteger(value.getEngine().ordinal()),
              CborSerializer.encodeByteString(value.encode()),
              CborSerializer.encodeByteString(value.encodeParameters())
          )
      );
    }
  }

  public static class Deserializer extends StdDeserializer<SerializablePredicate> {

    public Deserializer() {
      super(SerializablePredicate.class);
    }

    @Override
    public SerializablePredicate deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (p.getCurrentToken() != JsonToken.VALUE_STRING) {
        throw MismatchedInputException.from(
            p,
            EncodedPredicate.class,
            "Expected string value"
        );
      }

      try {
        return EncodedPredicate.fromCbor(p.readValueAs(byte[].class));
      } catch (Exception e) {
        throw MismatchedInputException.from(p, EncodedPredicate.class, "Expected bytes");
      }
    }
  }
}

