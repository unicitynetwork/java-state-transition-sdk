package org.unicitylabs.sdk.serializer.cbor.transaction;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.unicitylabs.sdk.address.Address;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenState;
import org.unicitylabs.sdk.transaction.TransferTransactionData;
import java.io.IOException;
import java.util.List;

public class TransferTransactionDataCbor {
  private TransferTransactionDataCbor() {
  }

  public static class Serializer extends JsonSerializer<TransferTransactionData> {

    @Override
    public void serialize(TransferTransactionData value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 6);
      gen.writeObject(value.getSourceState());
      gen.writeObject(value.getRecipient());
      gen.writeObject(value.getSalt());
      gen.writeObject(value.getDataHash());
      gen.writeObject(value.getMessage());
      gen.writeObject(value.getNametags());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<TransferTransactionData> {

    @Override
    public TransferTransactionData deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, TransferTransactionData.class, "Expected array value");
      }

      return new TransferTransactionData(
          p.readValueAs(TokenState.class),
          p.readValueAs(Address.class),
          p.readValueAs(byte[].class),
          p.readValueAs(DataHash.class),
          p.readValueAs(byte[].class),
          ctx.readValue(p, ctx.getTypeFactory().constructCollectionType(List.class, Token.class))
      );
    }
  }
}
