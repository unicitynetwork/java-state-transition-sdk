
package com.unicity.sdk.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.LeafValue;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.smt.MerkleTreePath;
import com.unicity.sdk.shared.util.HexConverter;

import java.math.BigInteger;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a proof of inclusion or non-inclusion in a sparse merkle tree.
 */
public class InclusionProof implements ISerializable {
    private final MerkleTreePath merkleTreePath;
    private final Authenticator authenticator;
    private final DataHash transactionHash;

    public InclusionProof(MerkleTreePath merkleTreePath, Authenticator authenticator, DataHash transactionHash) {
        if ((authenticator == null) != (transactionHash == null)) {
            throw new IllegalArgumentException("Authenticator and transaction hash must be both set or both null.");
        }
        this.merkleTreePath = merkleTreePath;
        this.authenticator = authenticator;
        this.transactionHash = transactionHash;
    }
    
    /**
     * Factory method for Jackson deserialization from JSON.
     * Matches TypeScript SDK's IInclusionProofJson interface.
     */
    @JsonCreator
    public static InclusionProof fromJson(
            @JsonProperty("merkleTreePath") MerkleTreePath merkleTreePath,
            @JsonProperty("authenticator") Authenticator authenticator,
            @JsonProperty("transactionHash") String transactionHashHex) {
        DataHash transactionHash = null;
        if (transactionHashHex != null) {
            transactionHash = DataHash.fromImprint(HexConverter.decode(transactionHashHex));
        }
        return new InclusionProof(merkleTreePath, authenticator, transactionHash);
    }

    public MerkleTreePath getMerkleTreePath() {
        return merkleTreePath;
    }

    public Authenticator getAuthenticator() {
        return authenticator;
    }

    public DataHash getTransactionHash() {
        return transactionHash;
    }

    public CompletableFuture<InclusionProofVerificationStatus> verify(BigInteger requestId) {
        if (authenticator != null && transactionHash != null) {
            return authenticator.verify(transactionHash)
                    .thenCompose(verified -> {
                        if (!verified) {
                            return CompletableFuture.completedFuture(InclusionProofVerificationStatus.NOT_AUTHENTICATED);
                        }

                        return LeafValue.create(authenticator, transactionHash)
                                .thenCompose(leafValue -> {
                                    // TODO: Check leaf value against merkle tree path
                                    return verifyPath(requestId);
                                });
                    });
        }

        return verifyPath(requestId);
    }

    private CompletableFuture<InclusionProofVerificationStatus> verifyPath(BigInteger requestId) {
        return merkleTreePath.verify(requestId)
                .thenApply(result -> {
                    if (!result.isPathValid()) {
                        return InclusionProofVerificationStatus.PATH_INVALID;
                    }
                    if (!result.isPathIncluded()) {
                        return InclusionProofVerificationStatus.PATH_NOT_INCLUDED;
                    }
                    return InclusionProofVerificationStatus.OK;
                });
    }

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        
        root.set("merkleTreePath", mapper.valueToTree(merkleTreePath.toJSON()));
        root.set("authenticator", authenticator != null ? mapper.valueToTree(authenticator.toJSON()) : null);
        root.put("transactionHash", transactionHash != null ? (String) transactionHash.toJSON() : null);
        
        return root;
    }

    @Override
    public byte[] toCBOR() {
        return CborEncoder.encodeArray(
            merkleTreePath.toCBOR(),
            authenticator != null ? authenticator.toCBOR() : CborEncoder.encodeNull(),
            transactionHash != null ? transactionHash.toCBOR() : CborEncoder.encodeNull()
        );
    }
}
