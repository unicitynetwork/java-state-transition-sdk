
package com.unicity.sdk.transaction;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.LeafValue;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.cbor.CustomCborDecoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.smt.path.MerkleTreePath;
import com.unicity.sdk.shared.util.HexConverter;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Represents a proof of inclusion or non-inclusion in a sparse merkle tree.
 */
public class InclusionProof {
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

    @JsonProperty("merkleTreePath")
    public MerkleTreePath getMerkleTreePath() {
        return merkleTreePath;
    }

    @JsonProperty("authenticator")
    public Authenticator getAuthenticator() {
        return authenticator;
    }

    @JsonProperty("transactionHash")
    public DataHash getTransactionHash() {
        return transactionHash;
    }
    
//    public CompletableFuture<InclusionProofVerificationStatus> verify(RequestId requestId) {
//        if (authenticator != null && transactionHash != null) {
//            return authenticator.verify(transactionHash)
//                    .thenCompose(verified -> {
//                        if (!verified) {
//                            return CompletableFuture.completedFuture(InclusionProofVerificationStatus.NOT_AUTHENTICATED);
//                        }
//
//                        return LeafValue.create(authenticator, transactionHash)
//                                .thenCompose(leafValue -> {
//                                    // Check if leaf value matches the first step's branch value
//                                    if (merkleTreePath.getSteps() != null && merkleTreePath.getSteps().size() > 0) {
//                                        MerkleTreePathStep firstStep = merkleTreePath.getSteps().get(0);
//                                        if (firstStep.getBranch() != null) {
//                                            // Compare the leaf value bytes with the branch value bytes
//                                            byte[] branchValue = firstStep.getBranch().getValue();
//                                            byte[] leafBytes = leafValue.getBytes();
//                                            if (!java.util.Arrays.equals(leafBytes, branchValue)) {
//                                                return CompletableFuture.completedFuture(InclusionProofVerificationStatus.PATH_NOT_INCLUDED);
//                                            }
//                                        }
//                                    }
//                                    return verifyPath(requestId);
//                                });
//                    });
//        }
//
//        return verifyPath(requestId);
//    }

//    private CompletableFuture<InclusionProofVerificationStatus> verifyPath(RequestId requestId) {
//        // Convert RequestId to BitString representation for merkle path verification
//        // This adds a leading 1 bit to preserve any leading zeros in the hash
//        BigInteger requestIdValue = requestId.toBitString().toBigInteger();
//
//        return merkleTreePath.verify(requestIdValue)
//                .thenApply(result -> {
//                    if (!result.isPathValid()) {
//                        return InclusionProofVerificationStatus.PATH_INVALID;
//                    }
//                    if (!result.isPathIncluded()) {
//                        return InclusionProofVerificationStatus.PATH_NOT_INCLUDED;
//                    }
//                    return InclusionProofVerificationStatus.OK;
//                });
//    }

//    @Override
//    public Object toJSON() {
//        ObjectMapper mapper = new ObjectMapper();
//        ObjectNode root = mapper.createObjectNode();
//
//        root.set("merkleTreePath", mapper.valueToTree(merkleTreePath.toJSON()));
//        root.set("authenticator", authenticator != null ? mapper.valueToTree(authenticator.toJSON()) : null);
//        root.put("transactionHash", transactionHash != null ? (String) transactionHash.toJSON() : null);
//
//        return root;
//    }
//
//    @Override
//    public byte[] toCBOR() {
//        return CborEncoder.encodeArray(
//            merkleTreePath.toCBOR(),
//            authenticator != null ? authenticator.toCBOR() : CborEncoder.encodeNull(),
//            transactionHash != null ? transactionHash.toCBOR() : CborEncoder.encodeNull()
//        );
//    }

    public Object toJSON() {
        return null;
    }

    public byte[] toCBOR() {
        return new byte[0];
    }
//
//    /**
//     * Deserialize InclusionProof from JSON.
//     * @param jsonNode JSON node containing inclusion proof
//     * @return InclusionProof instance
//     */
//    public static InclusionProof fromJSON(JsonNode jsonNode) throws Exception {
//        // Deserialize merkle tree path
//        JsonNode pathNode = jsonNode.get("merkleTreePath");
//        MerkleTreePath merkleTreePath = MerkleTreePath.fromJSON(pathNode);
//
//        // Deserialize authenticator (optional)
//        Authenticator authenticator = null;
//        if (jsonNode.has("authenticator") && !jsonNode.get("authenticator").isNull()) {
//            JsonNode authNode = jsonNode.get("authenticator");
//            authenticator = Authenticator.fromJSON(authNode);
//        }
//
//        // Deserialize transaction hash (optional)
//        DataHash transactionHash = null;
//        if (jsonNode.has("transactionHash") && !jsonNode.get("transactionHash").isNull()) {
//            String txHashHex = jsonNode.get("transactionHash").asText();
//            transactionHash = DataHash.fromJSON(txHashHex);
//        }
//
//        return new InclusionProof(merkleTreePath, authenticator, transactionHash);
//    }
    
//    /**
//     * Deserialize InclusionProof from CBOR.
//     * @param data CBOR-encoded bytes
//     * @return InclusionProof instance
//     */
//    public static InclusionProof fromCBOR(byte[] data) {
//        try {
//            CustomCborDecoder.DecodeResult result = CustomCborDecoder.decode(data, 0);
//            if (!(result.value instanceof List)) {
//                throw new RuntimeException("Expected array for InclusionProof");
//            }
//
//            List<?> array = (List<?>) result.value;
//            if (array.size() < 3) {
//                throw new RuntimeException("Invalid InclusionProof array size");
//            }
//
//            // Deserialize components
//            MerkleTreePath merkleTreePath = MerkleTreePath.fromCBOR((byte[]) array.get(0));
//
//            Authenticator authenticator = null;
//            if (array.get(1) != null && array.get(1) instanceof byte[]) {
//                authenticator = Authenticator.fromCBOR((byte[]) array.get(1));
//            }
//
//            DataHash transactionHash = null;
//            if (array.get(2) != null && array.get(2) instanceof byte[]) {
//                transactionHash = DataHash.fromCBOR((byte[]) array.get(2));
//            }
//
//            return new InclusionProof(merkleTreePath, authenticator, transactionHash);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to deserialize InclusionProof from CBOR", e);
//        }
//    }
}
