package com.unicity.sdk.serializer.cbor.transaction;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.Transaction;
import java.io.IOException;

public class TransactionCbor {

  private TransactionCbor() {
  }

  public static class Serializer extends JsonSerializer<Transaction> {

    @Override
    public void serialize(Transaction value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartArray(value, 2);
      gen.writeObject(value.getData());
      gen.writeObject(value.getInclusionProof());
      gen.writeEndArray();
    }
  }

  public static class Deserializer extends JsonDeserializer<Transaction<?>>  implements
      ContextualDeserializer {

    private final JavaType transactionType;

    public Deserializer() {
      this.transactionType = null;
    }

    private Deserializer(JavaType valueType) {
      this.transactionType = valueType;
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctx,
        BeanProperty property) {
      JavaType wrapperType = ctx.getContextualType();
      JavaType valueType = wrapperType != null ? wrapperType.containedType(0) : null;
      return new TransactionCbor.Deserializer(valueType);
    }

    @Override
    public Transaction<?> deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, Transaction.class, "Expected array value");
      }

      return new Transaction<>(
          p.getCodec().readValue(p, this.transactionType),
          p.readValueAs(InclusionProof.class)
      );
    }
  }
}
