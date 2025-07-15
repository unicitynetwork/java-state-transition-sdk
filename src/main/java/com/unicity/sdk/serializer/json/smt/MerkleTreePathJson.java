package com.unicity.sdk.serializer.json.smt;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.smt.path.MerkleTreePath;
import com.unicity.sdk.shared.smt.path.MerkleTreePathStep;
import com.unicity.sdk.transaction.InclusionProof;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MerkleTreePathJson {
    private static final String ROOT_HASH_FIELD = "root";
    private static final String STEPS_FIELD = "steps";

    private MerkleTreePathJson() {}

    public static class Serializer extends JsonSerializer<MerkleTreePath> {
        @Override
        public void serialize(MerkleTreePath value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }

            gen.writeStartObject();
            gen.writeObjectField(ROOT_HASH_FIELD, value.getRootHash());
            gen.writeObjectField(STEPS_FIELD, value.getSteps());
            gen.writeEndObject();
        }
    }

    public static class Deserializer extends JsonDeserializer<MerkleTreePath> {
        @Override
        public MerkleTreePath deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            DataHash rootHash = null;
            List<MerkleTreePathStep> steps = new ArrayList<>();
            Set<String> fields = new HashSet<>();

            if (!p.isExpectedStartObjectToken()) {
                ctx.handleUnexpectedToken(MerkleTreePath.class, p);
            }

            while (p.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = p.currentName();

                if (!fields.add(fieldName)) {
                    ctx.reportInputMismatch(MerkleTreePath.class, "Duplicate field: %s", fieldName);
                }

                p.nextToken();
                try {
                    switch (fieldName) {
                        case ROOT_HASH_FIELD:
                            if (p.currentToken() != JsonToken.VALUE_STRING) {
                                ctx.reportInputMismatch(MerkleTreePath.class, "Expected string value");
                            }
                            rootHash = p.readValueAs(DataHash.class);
                            break;
                        case STEPS_FIELD:
                            if (p.currentToken() != JsonToken.START_ARRAY) {
                                ctx.reportInputMismatch(MerkleTreePath.class, "Expected array value");
                            }

                            while (p.nextToken() != JsonToken.END_ARRAY) {
                                steps.add(p.readValueAs(MerkleTreePathStep.class));
                            }
                            break;
                    }
                } catch (Exception e) {
                    ctx.reportInputMismatch(MerkleTreePath.class, "Invalid value for field '%s': %s", fieldName, e.getMessage());
                }
            }

            Set<String> missingFields = new HashSet<>(Set.of(ROOT_HASH_FIELD, STEPS_FIELD));
            missingFields.removeAll(fields);
            if (!missingFields.isEmpty()) {
                ctx.reportInputMismatch(MerkleTreePath.class, "Missing required fields: %s", missingFields);
            }

            return new MerkleTreePath(rootHash, steps);
        }
    }
}
