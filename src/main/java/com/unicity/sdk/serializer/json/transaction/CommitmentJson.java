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
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.transaction.Commitment;
import com.unicity.sdk.transaction.TransactionData;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CommitmentJson {
    private static final String REQUEST_ID_FIELD = "requestId";
    private static final String TRANSACTION_DATA_FIELD = "transactionData";
    private static final String AUTHENTICATOR_FIELD = "authenticator";

    private CommitmentJson() {}

    public static class Serializer extends JsonSerializer<Commitment> {
        @Override
        public void serialize(Commitment value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }

            gen.writeStartObject();
            gen.writeObjectField(REQUEST_ID_FIELD, value.getRequestId());
            gen.writeObjectField(TRANSACTION_DATA_FIELD, value.getTransactionData());
            gen.writeObjectField(AUTHENTICATOR_FIELD, value.getAuthenticator());
            gen.writeEndObject();
        }
    }

    public static class Deserializer extends JsonDeserializer<Commitment<?>> implements
        ContextualDeserializer {

        private final JavaType resultType;

        public Deserializer() {
            this.resultType = null;
        }

        private Deserializer(JavaType valueType) {
            this.resultType = valueType;
        }

        @Override
        public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
            JavaType wrapperType = ctxt.getContextualType();
            JavaType valueType = wrapperType != null ? wrapperType.containedType(0) : null;
            return new Deserializer(valueType);
        }

        @Override
        public Commitment<?> deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            RequestId requestId = null;
            TransactionData<?> transactionData = null;
            Authenticator authenticator = null;

            Set<String> fields = new HashSet<>();

            if (!p.isExpectedStartObjectToken()) {
                throw MismatchedInputException.from(p, Commitment.class, "Expected object value");
            }

            while (p.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = p.currentName();

                if (!fields.add(fieldName)) {
                    throw MismatchedInputException.from(p, Commitment.class,
                        String.format("Duplicate field: %s", fieldName));
                }

                p.nextToken();
                try {
                    switch (fieldName) {
                        case REQUEST_ID_FIELD:
                            requestId = p.readValueAs(RequestId.class);
                            break;
                        case TRANSACTION_DATA_FIELD:
                            transactionData = p.getCodec().readValue(p, this.resultType);
                            break;
                        case AUTHENTICATOR_FIELD:
                            authenticator = p.readValueAs(Authenticator.class);
                            break;
                        default:
                            p.skipChildren();
                    }
                } catch (Exception e) {
                    throw MismatchedInputException.wrapWithPath(e, Commitment.class, fieldName);
                }
            }

            Set<String> missingFields = new HashSet<>(Set.of(REQUEST_ID_FIELD, TRANSACTION_DATA_FIELD, AUTHENTICATOR_FIELD));
            missingFields.removeAll(fields);
            if (!missingFields.isEmpty()) {
                throw MismatchedInputException.from(p, Commitment.class,
                    String.format("Missing required fields: %s", missingFields));
            }

            return new Commitment<>(requestId, transactionData, authenticator);
        }
    }
}
