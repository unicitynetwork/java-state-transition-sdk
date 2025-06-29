
package com.unicity.sdk.shared.smt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.util.HexConverter;

public class MerkleTreePathStep implements ISerializable {
    private final DataHash hash;
    private final boolean isRight;
    private final DataHash root;
    private final DataHash sibling;
    private final String path;
    private final NodeBranch branch;

    // Constructor for legacy compatibility
    public MerkleTreePathStep(DataHash hash, boolean isRight) {
        this.hash = hash;
        this.isRight = isRight;
        this.root = null;
        this.sibling = null;
        this.path = null;
        this.branch = null;
    }

    // Constructor for new implementation
    public MerkleTreePathStep(DataHash root, DataHash sibling, String path, NodeBranch branch) {
        this.hash = sibling; // For compatibility
        this.isRight = false; // Default value
        this.root = root;
        this.sibling = sibling;
        this.path = path;
        this.branch = branch;
    }
    
    public DataHash getHash() {
        return hash;
    }

    public boolean isRight() {
        return isRight;
    }

    public DataHash getRoot() {
        return root;
    }

    public DataHash getSibling() {
        return sibling;
    }

    public String getPath() {
        return path;
    }

    public NodeBranch getBranch() {
        return branch;
    }

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        
        if (path != null) {
            node.put("path", path);
        }
        if (sibling != null) {
            node.put("sibling", sibling.toJSON().toString());
        }
        if (branch != null) {
            node.put("branch", branch.toString());
        }
        
        return node;
    }

    @Override
    public byte[] toCBOR() {
        // TODO: Implement CBOR serialization
        return new byte[0];
    }
    
    /**
     * Deserialize MerkleTreePathStep from JSON.
     * @param jsonNode JSON node containing step data
     * @param root The root hash (only for first step)
     * @return MerkleTreePathStep instance
     */
    public static MerkleTreePathStep fromJSON(JsonNode jsonNode, DataHash root) {
        // Get path
        String path = null;
        if (jsonNode.has("path") && !jsonNode.get("path").isNull()) {
            path = jsonNode.get("path").asText();
        }
        
        // Get sibling
        DataHash sibling = null;
        if (jsonNode.has("sibling") && !jsonNode.get("sibling").isNull()) {
            sibling = DataHash.fromJSON(jsonNode.get("sibling").asText());
        }
        
        // Get branch
        NodeBranch branch = null;
        if (jsonNode.has("branch") && !jsonNode.get("branch").isNull()) {
            // For now, we'll skip branch deserialization as it's complex
            // In production, this would need proper implementation
        }
        
        return new MerkleTreePathStep(root, sibling, path, branch);
    }
}
