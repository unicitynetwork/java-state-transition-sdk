package com.unicity.sdk.shared.smt;

import com.fasterxml.jackson.databind.JsonNode;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.util.HexConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents a branch in a Merkle tree path step.
 * This class encapsulates an optional value that can be null.
 */
public class MerkleTreePathStepBranch {
    private final byte[] value;

    public MerkleTreePathStepBranch(byte[] value) {
        this.value = value != null ? Arrays.copyOf(value, value.length) : null;
    }

    public byte[] getValue() {
        return value != null ? Arrays.copyOf(value, value.length) : null;
    }

    /**
     * Create from JSON representation.
     * @param jsonNode JSON node, expected to be an array
     * @return MerkleTreePathStepBranch instance
     */
    public static MerkleTreePathStepBranch fromJSON(JsonNode jsonNode) {
        if (!jsonNode.isArray()) {
            throw new IllegalArgumentException("Expected array for MerkleTreePathStepBranch");
        }
        
        if (jsonNode.size() == 0) {
            return new MerkleTreePathStepBranch(null);
        }
        
        String hexValue = jsonNode.get(0).asText();
        return new MerkleTreePathStepBranch(HexConverter.decode(hexValue));
    }

    /**
     * Convert to JSON representation.
     * @return List representing the JSON array
     */
    public List<String> toJSON() {
        List<String> result = new ArrayList<>();
        if (value != null) {
            result.add(HexConverter.encode(value));
        }
        return result;
    }

    /**
     * Convert to CBOR representation.
     * @return CBOR-encoded bytes
     */
    public byte[] toCBOR() {
        return CborEncoder.encodeArray(Arrays.asList(
            CborEncoder.encodeOptional(value, CborEncoder::encodeByteString)
        ));
    }

    @Override
    public String toString() {
        return "MerkleTreePathStepBranch[" + (value != null ? HexConverter.encode(value) : "null") + "]";
    }
}