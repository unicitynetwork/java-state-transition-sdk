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
import com.unicity.sdk.transaction.InclusionProof;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransactionData;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class TransactionJson {

  private static final String DATA_FIELD = "data";
  private static final String INCLUSION_PROOF_FIELD = "inclusionProof";

  private TransactionJson() {
  }

  public static class Serializer extends JsonSerializer<Transaction> {

    @Override
    public void serialize(Transaction value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartObject();
      gen.writeObjectField(DATA_FIELD, value.getData());
      gen.writeObjectField(INCLUSION_PROOF_FIELD, value.getInclusionProof());

      gen.writeEndObject();
    }
  }

  public static class Deserializer extends JsonDeserializer<Transaction<?>> implements
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
      return new TransactionJson.Deserializer(valueType);
    }

    @Override
    public Transaction<?> deserialize(JsonParser p, DeserializationContext ctx)
        throws IOException {
      InclusionProof inclusionProof = null;
      TransactionData<?> data = null;

      Set<String> fields = new HashSet<>();

      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, Transaction.class, "Expected object value");
      }

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, Transaction.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();
        try {
          switch (fieldName) {
            case INCLUSION_PROOF_FIELD:
              inclusionProof = p.readValueAs(InclusionProof.class);
              break;
            case DATA_FIELD:
              data = p.getCodec().readValue(p, this.transactionType);
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, Transaction.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(Set.of(INCLUSION_PROOF_FIELD, DATA_FIELD));
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, Transaction.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new Transaction<>(data, inclusionProof);
    }
  }
}
