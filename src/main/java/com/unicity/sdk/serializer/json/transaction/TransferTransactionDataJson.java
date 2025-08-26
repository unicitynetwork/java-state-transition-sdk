package com.unicity.sdk.serializer.json.transaction;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.address.Address;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.transaction.TransferTransactionData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TransferTransactionDataJson {

  // TODO: Include token id and type within each transfer transaction data so it would be easier to handle hashing and parsing
  private static final String SOURCE_STATE_FIELD = "state";
  private static final String RECIPIENT_FIELD = "recipient";
  private static final String SALT_FIELD = "salt";
  private static final String DATA_HASH_FIELD = "dataHash";
  private static final String MESSAGE_FIELD = "message";
  private static final String NAMETAG_FIELD = "nametags";

  private TransferTransactionDataJson() {
  }

  public static class Serializer extends JsonSerializer<TransferTransactionData> {

    @Override
    public void serialize(TransferTransactionData value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartObject();
      gen.writeObjectField(DATA_HASH_FIELD, value.getDataHash());
      gen.writeObjectField(MESSAGE_FIELD, value.getMessage());
      gen.writeObjectField(RECIPIENT_FIELD, value.getRecipient());
      gen.writeObjectField(SALT_FIELD, value.getSalt());
      gen.writeObjectField(SOURCE_STATE_FIELD, value.getSourceState());
      gen.writeObjectField(NAMETAG_FIELD, value.getNametags());
      gen.writeEndObject();
    }
  }

  public static class Deserializer extends JsonDeserializer<TransferTransactionData> {

    @Override
    public TransferTransactionData deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, TransferTransactionData.class,
            "Expected object value");
      }

      TokenState tokenState = null;
      Address recipient = null;
      byte[] salt = null;
      DataHash dataHash = null;
      byte[] message = null;
      List<Token<?>> nametags = new ArrayList<>();

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();
        p.nextToken(); // Move to the value

        switch (fieldName) {
          case SOURCE_STATE_FIELD:
            tokenState = p.readValueAs(TokenState.class);
            break;
          case RECIPIENT_FIELD:
            recipient = p.readValueAs(Address.class);
            break;
          case SALT_FIELD:
            salt = p.readValueAs(byte[].class);
            break;
          case DATA_HASH_FIELD:
            dataHash = p.readValueAs(DataHash.class);
            break;
          case MESSAGE_FIELD:
            message = p.readValueAs(byte[].class);
            break;
          case NAMETAG_FIELD:
            if (!p.isExpectedStartArrayToken()) {
              throw MismatchedInputException.from(p, Token.class, "Expected array value");
            }

            while (p.nextToken() != JsonToken.END_ARRAY) {
              nametags.add(p.readValueAs(Token.class));
            }
            break;
          default:
            p.skipChildren(); // Skip unknown fields
        }
      }

      return new TransferTransactionData(tokenState, recipient, salt, dataHash, message, nametags);
    }
  }
}
