package com.unicity.sdk.serializer.json.transaction;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.smt.path.MerkleTreePath;
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
                throw MismatchedInputException.from(p, InclusionProof.class, "Expected object value");
            }

            while (p.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = p.currentName();

                if (!fields.add(fieldName)) {
                    throw MismatchedInputException.from(p, InclusionProof.class, String.format("Duplicate field: %s", fieldName));
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
                        default:
                            p.skipChildren();
                    }
                } catch (Exception e) {
                    throw MismatchedInputException.wrapWithPath(e, InclusionProof.class, fieldName);
                }
            }

            if (merkleTreePath == null) {
                throw MismatchedInputException.from(p, InclusionProof.class, String.format("Missing required fields: %s", MERKLE_TREE_PATH_FIELD));
            }

            return new InclusionProof(merkleTreePath, authenticator, transactionHash);
        }
    }
}
