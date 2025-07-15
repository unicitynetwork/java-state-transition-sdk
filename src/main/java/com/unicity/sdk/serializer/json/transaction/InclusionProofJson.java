package com.unicity.sdk.serializer.json.transaction;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.smt.path.MerkleTreePath;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.transaction.InclusionProof;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class InclusionProofJson {
    private static final String MERKLE_TREE_PATH_FIELD = "merkleTreePath";
    private static final String AUTHENTICATOR_FIELD = "authenticator";
    private static final String TRANSACTION_HASH_FIELD = "transactionHash";

    private InclusionProofJson() {}

    public static class Serializer extends JsonSerializer<InclusionProof> {
        @Override
        public void serialize(InclusionProof value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }

            gen.writeStartObject();
            gen.writeObjectField(MERKLE_TREE_PATH_FIELD, value.getMerkleTreePath());
            gen.writeObjectField(AUTHENTICATOR_FIELD, value.getAuthenticator());
            gen.writeObjectField(TRANSACTION_HASH_FIELD, value.getTransactionHash());
            gen.writeEndObject();
        }
    }

    public static class Deserializer extends JsonDeserializer<InclusionProof> {
        @Override
        public InclusionProof deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            MerkleTreePath merkleTreePath = null;
            Authenticator authenticator = null;
            DataHash transactionHash = null;

            Set<String> fields = new HashSet<>();

            if (!p.isExpectedStartObjectToken()) {
                ctx.handleUnexpectedToken(InclusionProof.class, p);
            }

            while (p.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = p.currentName();

                if (!fields.add(fieldName)) {
                    ctx.reportInputMismatch(InclusionProof.class, "Duplicate field: %s", fieldName);
                }

                p.nextToken();
                try {
                    switch (fieldName) {
                        case MERKLE_TREE_PATH_FIELD:
                            merkleTreePath = p.readValueAs(MerkleTreePath.class);
                            break;
                        case AUTHENTICATOR_FIELD:
                            authenticator = p.currentToken() != JsonToken.VALUE_NULL ? p.readValueAs(Authenticator.class) : null;
                            break;
                        case TRANSACTION_HASH_FIELD:
                            transactionHash = p.currentToken() != JsonToken.VALUE_NULL ? p.readValueAs(DataHash.class) : null;
                            break;
                    }
                } catch (Exception e) {
                    ctx.reportInputMismatch(InclusionProof.class, "Invalid value for field '%s': %s", fieldName, e.getMessage());
                }
            }

            if (merkleTreePath == null) {
                ctx.reportInputMismatch(InclusionProof.class, "Missing required fields: %s", MERKLE_TREE_PATH_FIELD);
            }

            return new InclusionProof(merkleTreePath, authenticator, transactionHash);
        }
    }
}
