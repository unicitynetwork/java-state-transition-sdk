package org.unicitylabs.sdk.address;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.unicitylabs.sdk.predicate.EncodedPredicate;

/**
 * Address serializer and deserializer implementation.
 */
public class AddressJson {

  private AddressJson() {
  }

  /**
   * Address serializer.
   */
  public static class Serializer extends StdSerializer<Address> {

    /**
     * Create serializer.
     */
    public Serializer() {
      super(Address.class);
    }

    /**
     * Serialize address.
     *
     * @param value       addess
     * @param gen         json generator
     * @param serializers serializer provider
     * @throws IOException on serialization failure
     */
    @Override
    public void serialize(Address value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      gen.writeObject(value.toString());
    }
  }

  /**
   * Address deserializer.
   */
  public static class Deserializer extends StdDeserializer<Address> {

    /**
     * Create deserializer.
     */
    public Deserializer() {
      super(Address.class);
    }

    /**
     * Deserialize address.
     *
     * @param p   Parser used for reading JSON content
     * @param ctx Context that can be used to access information about this deserialization
     *            activity.
     * @return address
     * @throws IOException on deserialization failure
     */
    @Override
    public Address deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (p.getCurrentToken() != JsonToken.VALUE_STRING) {
        throw MismatchedInputException.from(
            p,
            EncodedPredicate.class,
            "Expected string value"
        );
      }

      try {
        return AddressFactory.createAddress(p.readValueAs(String.class));
      } catch (Exception e) {
        throw MismatchedInputException.from(p, EncodedPredicate.class, "Expected bytes");
      }
    }
  }
}

