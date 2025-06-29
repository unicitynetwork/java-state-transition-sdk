
package com.unicity.sdk.shared.smt;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.util.HexConverter;

public class MerkleTreePathStep {
    private final DataHash hash;
    private final boolean isRight;

    public MerkleTreePathStep(DataHash hash, boolean isRight) {
        this.hash = hash;
        this.isRight = isRight;
    }
    
    /**
     * Factory method for Jackson deserialization from JSON.
     */
    @JsonCreator
    public static MerkleTreePathStep fromJson(
            @JsonProperty("hash") String hashHex,
            @JsonProperty("isRight") boolean isRight) {
        if (hashHex == null) {
            return new MerkleTreePathStep(null, isRight);
        }
        DataHash hash = DataHash.fromImprint(HexConverter.decode(hashHex));
        return new MerkleTreePathStep(hash, isRight);
    }

    public DataHash getHash() {
        return hash;
    }

    public boolean isRight() {
        return isRight;
    }
}
