package com.unicity.sdk.serializer.json.transaction;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.address.Address;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.MintTransactionReason;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class MintTransactionDataJson {

  private static final String TOKEN_ID_FIELD = "tokenId";
  private static final String TOKEN_TYPE_FIELD = "tokenType";
  private static final String TOKEN_DATA_FIELD = "tokenData";
  private static final String COIN_DATA_FIELD = "coinData";
  private static final String RECIPIENT_FIELD = "recipient";
  private static final String SALT_FIELD = "salt";
  private static final String DATA_HASH_FIELD = "dataHash";
  private static final String REASON_FIELD = "reason";

  private MintTransactionDataJson() {
  }

  public static class Serializer extends JsonSerializer<MintTransactionData> {

    @Override
    public void serialize(MintTransactionData value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartObject();
      gen.writeObjectField(TOKEN_ID_FIELD, value.getTokenId());
      gen.writeObjectField(TOKEN_TYPE_FIELD, value.getTokenType());
      gen.writeObjectField(TOKEN_DATA_FIELD, value.getTokenData());
      gen.writeObjectField(COIN_DATA_FIELD, value.getCoinData());
      gen.writeObjectField(RECIPIENT_FIELD, value.getRecipient());
      gen.writeObjectField(SALT_FIELD, value.getSalt());
      gen.writeObjectField(DATA_HASH_FIELD, value.getDataHash());
      gen.writeObjectField(REASON_FIELD, value.getReason());
      gen.writeEndObject();
    }
  }

  public static class Deserializer extends
      JsonDeserializer<MintTransactionData<MintTransactionReason>> {

    @Override
    public MintTransactionData<MintTransactionReason> deserialize(JsonParser p,
        DeserializationContext ctx)
        throws IOException {
      TokenId tokenId = null;
      TokenType tokenType = null;
      byte[] tokenData = null;
      TokenCoinData coinData = null;
      Address recipient = null;
      byte[] salt = null;
      DataHash dataHash = null;
      MintTransactionReason reason = null;

      Set<String> fields = new HashSet<>();

      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, MintTransactionData.class, "Expected object value");
      }

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, MintTransactionData.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();
        try {
          switch (fieldName) {
            case TOKEN_ID_FIELD:
              tokenId = p.readValueAs(TokenId.class);
              break;
            case TOKEN_TYPE_FIELD:
              tokenType = p.readValueAs(TokenType.class);
              break;
            case TOKEN_DATA_FIELD:
              tokenData = p.readValueAs(byte[].class);
              break;
            case COIN_DATA_FIELD:
              coinData = p.readValueAs(TokenCoinData.class);
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
            case REASON_FIELD:
              reason = p.readValueAs(MintTransactionReason.class);
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, MintTransactionData.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(Set.of(
          TOKEN_ID_FIELD, TOKEN_TYPE_FIELD, TOKEN_DATA_FIELD, RECIPIENT_FIELD, SALT_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, MintTransactionData.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new MintTransactionData<>(tokenId, tokenType, tokenData, coinData, recipient, salt,
          dataHash, reason);
    }
  }
}
