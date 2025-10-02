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

/**
 * Predicate serializer and deserializer implementation.
 */
public class SerializablePredicateJson {

  private SerializablePredicateJson() {
  }

  /**
   * Predicate serializer.
   */
  public static class Serializer extends StdSerializer<SerializablePredicate> {

    /**
     * Create predicate serializer.
     */
    public Serializer() {
      super(SerializablePredicate.class);
    }

    /**
     * Serialize predicate.
     *
     * @param value       predicate
     * @param gen         json generator
     * @param serializers serializer provider
     * @throws IOException on serialization failure
     */
    @Override
    public void serialize(SerializablePredicate value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      gen.writeObject(
          CborSerializer.encodeArray(
              CborSerializer.encodeUnsignedInteger(value.getEngine().ordinal()),
              CborSerializer.encodeByteString(value.encode()),
              CborSerializer.encodeByteString(value.encodeParameters())
          )
      );
    }
  }

  /**
   * Predicate deserializer.
   */
  public static class Deserializer extends StdDeserializer<SerializablePredicate> {

    /**
     * Create predicate deserializer.
     */
    public Deserializer() {
      super(SerializablePredicate.class);
    }

    /**
     * Deserialize predicate.
     *
     * @param p   Parser used for reading JSON content
     * @param ctx Context that can be used to access information about this deserialization
     *            activity.
     * @return predicate
     * @throws IOException on deserialization failure
     */
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

