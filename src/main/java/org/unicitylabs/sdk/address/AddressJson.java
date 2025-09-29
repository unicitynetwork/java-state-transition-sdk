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
import org.unicitylabs.sdk.predicate.SerializablePredicate;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;

public class AddressJson {

  private AddressJson() {
  }

  public static class Serializer extends StdSerializer<Address> {

    public Serializer() {
      super(Address.class);
    }

    @Override
    public void serialize(Address value, JsonGenerator gen,
        SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeObject(value.toString());
    }
  }

  public static class Deserializer extends StdDeserializer<Address> {

    public Deserializer() {
      super(Address.class);
    }

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

