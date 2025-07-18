
package com.unicity.sdk.shared.smt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.hash.JavaDataHasher;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MerkleTreePath implements ISerializable {
    private final DataHash root;
    private final List<MerkleTreePathStep> steps;

    @JsonCreator
    public MerkleTreePath(@JsonProperty("root") DataHash root, @JsonProperty("steps") List<MerkleTreePathStep> steps) {
        this.root = root;
        this.steps = steps;
    }

    public DataHash getRoot() {
        return root;
    }
    
    public List<MerkleTreePathStep> getSteps() {
        return steps;
    }

    public CompletableFuture<MerkleTreePathVerificationResult> verify(BigInteger requestId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                BigInteger currentPath = BigInteger.ONE;
                DataHash currentHash = null;
                
                for (int i = 0; i < steps.size(); i++) {
                    MerkleTreePathStep step = steps.get(i);
                    
                    byte[] hash;
                    
                    if (step.getBranch() == null) {
                        hash = new byte[]{0};
                    } else {
                        // Branch is not null, but value might be null
                        byte[] bytes = i == 0 ? step.getBranch().getValue() : 
                                      (currentHash != null ? currentHash.getHash() : null);
                        if (bytes == null) {
                            bytes = new byte[]{0};
                        }
                        
                        // Create hasher and compute hash
                        JavaDataHasher hasher = new JavaDataHasher(HashAlgorithm.SHA256);
                        hasher.update(step.getPath().toByteArray());
                        hasher.update(bytes);
                        DataHash digest = hasher.digest().join();
                        hash = digest.getHash();
                        
                        // Update current path after hashing (as per TypeScript implementation)
                        // This happens when branch is not null (regardless of whether value is null)
                        // length = step.path.toString(2).length - 1
                        // currentPath = (currentPath << length) | (step.path & ((1 << length) - 1))
                        String pathBinary = step.getPath().toString(2);
                        int length = pathBinary.length() - 1;
                        if (length > 0) {
                            BigInteger mask = BigInteger.ONE.shiftLeft(length).subtract(BigInteger.ONE);
                            BigInteger maskedPath = step.getPath().and(mask);
                            currentPath = currentPath.shiftLeft(length).or(maskedPath);
                        }
                    }
                    
                    // Get sibling hash
                    byte[] siblingHash = step.getSibling() != null ? step.getSibling().getHash() : new byte[]{0};
                    boolean isRight = step.getPath().and(BigInteger.ONE).equals(BigInteger.ONE);
                    
                    // Compute new hash
                    JavaDataHasher hasher = new JavaDataHasher(HashAlgorithm.SHA256);
                    if (isRight) {
                        hasher.update(siblingHash);
                        hasher.update(hash);
                    } else {
                        hasher.update(hash);
                        hasher.update(siblingHash);
                    }
                    currentHash = hasher.digest().join();
                }
                
                boolean pathValid = currentHash != null && root.equals(currentHash);
                boolean pathIncluded = requestId.equals(currentPath);
                
                return new MerkleTreePathVerificationResult(pathValid, pathIncluded);
            } catch (Exception e) {
                // In case of any error, return invalid result
                e.printStackTrace();
                return new MerkleTreePathVerificationResult(false, false);
            }
        });
    }

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode rootNode = mapper.createObjectNode();
        
        // Add root hash
        if (root != null) {
            rootNode.put("root", root.toJSON().toString());
        }
        
        ArrayNode stepsArray = mapper.createArrayNode();
        for (MerkleTreePathStep step : steps) {
            stepsArray.add(mapper.valueToTree(step.toJSON()));
        }
        rootNode.set("steps", stepsArray);
        
        return rootNode;
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
                MerkleTreePathStep step = MerkleTreePathStep.fromJSON(stepNode);
                steps.add(step);
            }
        }
        
        return new MerkleTreePath(root, steps);
    }

    public static class MerkleTreePathVerificationResult {
        private final boolean pathValid;
        private final boolean pathIncluded;
        private final boolean result;

        public MerkleTreePathVerificationResult(boolean pathValid, boolean pathIncluded) {
            this.pathValid = pathValid;
            this.pathIncluded = pathIncluded;
            this.result = pathValid && pathIncluded;
        }

        public boolean isPathValid() {
            return pathValid;
        }

        public boolean isPathIncluded() {
            return pathIncluded;
        }
        
        public boolean getResult() {
            return result;
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
