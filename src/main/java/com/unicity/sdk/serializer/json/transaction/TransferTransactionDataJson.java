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
import com.unicity.sdk.address.ProxyAddress;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.serializer.json.token.TokenStateJson;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.MintTransactionReason;
import com.unicity.sdk.transaction.TransferTransactionData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TransferTransactionDataJson {

  private static final String SOURCE_STATE_FIELD = "state";
  private static final String RECIPIENT_FIELD = "recipient";
  private static final String SALT_FIELD = "salt";
  private static final String DATA_HASH_FIELD = "dataHash";
  private static final String MESSAGE_FIELD = "message";
  private static final String NAMETAG_FIELD = "nametagTokens";

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
      gen.writeObjectField(NAMETAG_FIELD, List.copyOf(value.getNametags().values()));
      gen.writeEndObject();
    }
  }

  public static class Deserializer extends
      JsonDeserializer<TransferTransactionData> {

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
      Map<Address, Token<?>> nametags = new HashMap<>();

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
            if (p.currentToken() != JsonToken.START_ARRAY) {
              throw MismatchedInputException.from(p, Token.class, "Expected array value");
            }

            while (p.nextToken() != JsonToken.END_ARRAY) {
              Token<?> token = p.readValueAs(Token.class);
              Address address = ProxyAddress.create(token.getId());
              if (nametags.containsKey(address)) {
                throw MismatchedInputException.from(p, Token.class, "Duplicate nametag");
              }

              nametags.put(address, token);
            }
            break;
          default:
            p.skipChildren(); // Skip unknown fields
        }
      }

      return new TransferTransactionData(
          tokenState,
          recipient,
          salt,
          dataHash,
          message,
          nametags
      );
    }
  }
}
