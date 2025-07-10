
package com.unicity.sdk.shared.smt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.cbor.CborDecoder;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.hash.DataHash;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MerkleTreePath implements ISerializable {
    private final List<MerkleTreePathStep> steps;

    @JsonCreator
    public MerkleTreePath(@JsonProperty("steps") List<MerkleTreePathStep> steps) {
        this.steps = steps;
    }

    public List<MerkleTreePathStep> getSteps() {
        return steps;
    }

    public CompletableFuture<MerkleTreePathVerificationResult> verify(BigInteger requestId) {
        // TODO: Implement merkle tree path verification
        return CompletableFuture.completedFuture(new MerkleTreePathVerificationResult(true, true));
    }

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        
        // Add root hash if first step has it
        if (!steps.isEmpty() && steps.get(0).getRoot() != null) {
            root.put("root", steps.get(0).getRoot().toJSON().toString());
        }
        
        ArrayNode stepsArray = mapper.createArrayNode();
        for (MerkleTreePathStep step : steps) {
            stepsArray.add(mapper.valueToTree(step.toJSON()));
        }
        root.set("steps", stepsArray);
        
        return root;
    }

    @Override
    public byte[] toCBOR() {
        // TODO: Implement CBOR serialization
        return CborEncoder.encodeNull();
    }
    
    /**
     * Deserialize MerkleTreePath from JSON.
     * @param jsonNode JSON node containing merkle tree path
     * @return MerkleTreePath instance
     */
    public static MerkleTreePath fromJSON(JsonNode jsonNode) {
        List<MerkleTreePathStep> steps = new ArrayList<>();
        
        // Get root if present
        DataHash root = null;
        if (jsonNode.has("root") && !jsonNode.get("root").isNull()) {
            root = DataHash.fromJSON(jsonNode.get("root").asText());
        }
        
        // Get steps
        JsonNode stepsNode = jsonNode.get("steps");
        if (stepsNode != null && stepsNode.isArray()) {
            for (JsonNode stepNode : stepsNode) {
                MerkleTreePathStep step = MerkleTreePathStep.fromJSON(stepNode, root);
                steps.add(step);
                root = null; // Only first step has root
            }
        }
        
        return new MerkleTreePath(steps);
    }

    public static class MerkleTreePathVerificationResult {
        private final boolean pathValid;
        private final boolean pathIncluded;

        public MerkleTreePathVerificationResult(boolean pathValid, boolean pathIncluded) {
            this.pathValid = pathValid;
            this.pathIncluded = pathIncluded;
        }

        public boolean isPathValid() {
            return pathValid;
        }

        public boolean isPathIncluded() {
            return pathIncluded;
        }
    }
    
    /**
     * Deserialize MerkleTreePath from CBOR.
     * @param cbor The CBOR-encoded bytes
     * @return A MerkleTreePath instance
     */
    public static MerkleTreePath fromCBOR(byte[] cbor) {
        // TODO: Implement CBOR deserialization for MerkleTreePath
        // This is a placeholder implementation
        throw new UnsupportedOperationException("MerkleTreePath CBOR deserialization not yet implemented");
    }
}
