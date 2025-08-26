package com.unicity.sdk.serializer.cbor.token;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenState;
import com.unicity.sdk.transaction.MintTransactionData;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransferTransactionData;
import java.io.IOException;
import java.util.List;

public class TokenCbor {

  private TokenCbor() {
  }

  public static class Serializer extends JsonSerializer<Token> {

    @Override
    public void serialize(Token value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 5);
      gen.writeObject(value.getVersion());
      gen.writeObject(value.getGenesis());
      List<Transaction<?>> transactions = value.getTransactions();
      gen.writeStartArray(value, transactions.size());
      for (Transaction<?> transaction : transactions) {
        gen.writeObject(transaction);
      }
      gen.writeEndArray();
      gen.writeObject(value.getState());
      gen.writeObject(value.getNametags());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends
      JsonDeserializer<Token<Transaction<MintTransactionData<?>>>> {

    @Override
    public Token<Transaction<MintTransactionData<?>>> deserialize(JsonParser p,
        DeserializationContext ctx) throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, Token.class, "Expected array value");
      }

      return new Token<>(
          p.readValueAs(TokenState.class),
          ctx.readValue(p, ctx.getTypeFactory().constructParametricType(Transaction.class, MintTransactionData.class)),
          ctx.readValue(p, ctx.getTypeFactory().constructCollectionType(List.class, ctx.getTypeFactory().constructParametricType(Transaction.class, TransferTransactionData.class))),
          ctx.readValue(p, ctx.getTypeFactory().constructCollectionType(List.class, Token.class))
      );
    }
  }
}
