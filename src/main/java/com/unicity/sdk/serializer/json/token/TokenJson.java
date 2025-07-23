package com.unicity.sdk.serializer.json.token;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.address.Address;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.smt.path.MerkleTreePath;
import com.unicity.sdk.smt.path.MerkleTreePathStep;
import com.unicity.sdk.token.NameTagToken;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.token.fungible.TokenCoinData;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.MintTransactionReason;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransactionData;
import com.unicity.sdk.transaction.TransferTransactionData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TokenJson {

  private static final String STATE_FIELD = "state";
  private static final String GENESIS_FIELD = "genesis";
  private static final String TRANSACTIONS_FIELD = "transactions";
  private static final String NAMETAG_FIELD = "nametagTokens";

  private TokenJson() {
  }

  public static class Serializer extends JsonSerializer<Token> {

    @Override
    public void serialize(Token value, JsonGenerator gen,
        SerializerProvider serializers) throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartObject();
      gen.writeObjectField(STATE_FIELD, value.getState());
      gen.writeObjectField(GENESIS_FIELD, value.getGenesis());
      gen.writeObjectField(TRANSACTIONS_FIELD, value.getTransactions());
      gen.writeObjectField(NAMETAG_FIELD, value.getNametagTokens());
      gen.writeEndObject();
    }
  }

  public static class Deserializer extends
      JsonDeserializer<Token<Transaction<MintTransactionData<?>>>> {

    @Override
    public Token<Transaction<MintTransactionData<?>>> deserialize(JsonParser p,
        DeserializationContext ctx)
        throws IOException {
      TokenState state = null;
      Transaction<MintTransactionData<?>> genesis = null;
      List<Transaction<TransferTransactionData>> transactions = new ArrayList<>();
      List<NameTagToken> nametagTokens = new ArrayList<>();

      Set<String> fields = new HashSet<>();

      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, Token.class, "Expected object value");
      }

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, Token.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();
        try {
          switch (fieldName) {
            case STATE_FIELD:
              state = p.readValueAs(TokenState.class);
              break;
            case GENESIS_FIELD:
              genesis = ctx.readValue(p, ctx.getTypeFactory()
                  .constructParametricType(Transaction.class, MintTransactionData.class));
              break;
            case TRANSACTIONS_FIELD:
              if (p.currentToken() != JsonToken.START_ARRAY) {
                throw MismatchedInputException.from(p, Token.class, "Expected array value");
              }

              while (p.nextToken() != JsonToken.END_ARRAY) {
                transactions.add(
                    ctx.readValue(p, ctx.getTypeFactory()
                        .constructParametricType(Transaction.class, TransferTransactionData.class))
                );
              }
              break;
            case NAMETAG_FIELD:
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, Token.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(Set.of(
          STATE_FIELD, GENESIS_FIELD, TRANSACTIONS_FIELD, NAMETAG_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, Token.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new Token<>(state, genesis, transactions, nametagTokens);
    }
  }
}
