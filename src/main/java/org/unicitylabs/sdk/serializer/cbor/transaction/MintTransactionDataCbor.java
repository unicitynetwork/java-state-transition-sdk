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
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.token.fungible.TokenCoinData;
import org.unicitylabs.sdk.transaction.MintTransactionData;
import org.unicitylabs.sdk.transaction.MintTransactionReason;
import java.io.IOException;

public class MintTransactionDataCbor {

  private MintTransactionDataCbor() {
  }

  public static class Serializer extends JsonSerializer<MintTransactionData> {

    @Override
    public void serialize(MintTransactionData value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 8);
      gen.writeObject(value.getTokenId());
      gen.writeObject(value.getTokenType());
      gen.writeObject(value.getTokenData());
      gen.writeObject(value.getCoinData());
      gen.writeObject(value.getRecipient());
      gen.writeObject(value.getSalt());
      gen.writeObject(value.getDataHash());
      gen.writeObject(value.getReason());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends
      JsonDeserializer<MintTransactionData<MintTransactionReason>> {

    @Override
    public MintTransactionData<MintTransactionReason> deserialize(JsonParser p,
        DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, MintTransactionData.class, "Expected array value");
      }

      return new MintTransactionData<>(
          p.readValueAs(TokenId.class),
          p.readValueAs(TokenType.class),
          p.readValueAs(byte[].class),
          p.readValueAs(TokenCoinData.class),
          p.readValueAs(Address.class),
          p.readValueAs(byte[].class),
          p.readValueAs(DataHash.class),
          p.readValueAs(MintTransactionReason.class)
      );
    }
  }
}
