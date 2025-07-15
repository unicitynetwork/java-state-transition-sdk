package com.unicity.sdk.serializer.json.smt;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.*;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.smt.path.MerkleTreePathStepBranch;
import com.unicity.sdk.shared.util.HexConverter;

import java.io.IOException;

public class MerkleTreePathStepBranchJson {
    private MerkleTreePathStepBranchJson() {
    }

    public static class Serializer extends JsonSerializer<MerkleTreePathStepBranch> {
        @Override
        public void serialize(MerkleTreePathStepBranch value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            if (value == null) {
                gen.writeNull();
                return;
            }

            String[] result = value.getValue() == null ? new String[0] : new String[]{HexConverter.encode(value.getValue())};
            gen.writeArray(result, 0, result.length);
        }
    }

    public static class Deserializer extends JsonDeserializer<MerkleTreePathStepBranch> {
        @Override
        public MerkleTreePathStepBranch deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
            if (!p.isExpectedStartArrayToken()) {
                ctx.handleUnexpectedToken(MerkleTreePathStepBranch.class, p);
            }

            p.nextToken();
            String value = p.currentToken() != JsonToken.VALUE_NULL ? p.getValueAsString() : null;
            if (p.nextToken() != JsonToken.END_ARRAY) {
                ctx.handleUnexpectedToken(MerkleTreePathStepBranch.class, p);
            }

            try {
                return new MerkleTreePathStepBranch(value == null ? null : HexConverter.decode(value));
            } catch (Exception e) {
                ctx.reportInputMismatch(DataHash.class, "Expected string value");
                throw e;
            }
        }
    }
}
