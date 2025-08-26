package com.unicity.sdk.serializer.cbor.address;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.address.Address;
import com.unicity.sdk.address.AddressFactory;
import java.io.IOException;

public class AddressCbor {

  private AddressCbor() {
  }

  public static class Serializer extends JsonSerializer<Address> {

    public Serializer() {
    }

    @Override
    public void serialize(Address value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeString(value.getAddress());
    }
  }

  public static class Deserializer extends JsonDeserializer<Address> {

    public Deserializer() {
    }

    @Override
    public Address deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      if (p.getCurrentToken() != JsonToken.VALUE_STRING) {
        throw MismatchedInputException.from(p, Address.class,
            "Expected string value");
      }

      try {
        return AddressFactory.createAddress(p.readValueAs(String.class));
      } catch (Exception e) {
        throw MismatchedInputException.from(p, Address.class,
            String.format("Invalid address: %s", e.getMessage()));
      }
    }
  }
}
