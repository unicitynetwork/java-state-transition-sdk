
package com.unicity.sdk.predicate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.util.HexConverter;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class UnmaskedPredicate extends DefaultPredicate {
    private final TokenType tokenType;

    private UnmaskedPredicate(
            DataHash hash,
            DataHash reference,
            byte[] publicKey,
            String algorithm,
            HashAlgorithm hashAlgorithm,
            byte[] nonce,
            TokenType tokenType) {
        super(PredicateType.UNMASKED, publicKey, algorithm, hashAlgorithm, nonce, reference, hash);
        this.tokenType = tokenType;
    }

    /**
     * Create an unmasked predicate with signing service.
     */
    public static CompletableFuture<UnmaskedPredicate> create(
            TokenId tokenId,
            TokenType tokenType,
            com.unicity.sdk.shared.signing.SigningService signingService,
            HashAlgorithm hashAlgorithm,
            byte[] nonce) {
        try {
            // Calculate reference
            DataHash reference = calculateReference(tokenType, signingService.getAlgorithm(), 
                signingService.getPublicKey(), hashAlgorithm);
            
            // Calculate hash
            DataHash hash = calculateHash(reference, tokenId.getBytes(), nonce, hashAlgorithm);
            
            return CompletableFuture.completedFuture(
                new UnmaskedPredicate(hash, reference, signingService.getPublicKey(), 
                    signingService.getAlgorithm(), hashAlgorithm, nonce, tokenType)
            );
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Create an unmasked predicate from public key.
     */
    public static CompletableFuture<UnmaskedPredicate> createFromPublicKey(
            byte[] tokenId,
            TokenType tokenType,
            String algorithm,
            byte[] publicKey,
            HashAlgorithm hashAlgorithm) {
        try {
            // Calculate nonce by signing a salt hash
            DataHasher nonceHasher = new DataHasher(HashAlgorithm.SHA256);
            nonceHasher.update(publicKey);
            nonceHasher.update(tokenId);
            DataHash saltHash = nonceHasher.digest().get();
            
            // For unmasked predicate, we'll use the salt hash bytes as nonce
            // In production, this would be signed by the private key
            byte[] nonce = saltHash.getData();
            
            // Calculate reference
            DataHash reference = calculateReference(tokenType, algorithm, publicKey, hashAlgorithm);
            
            // Calculate hash
            DataHash hash = calculateHash(reference, tokenId, nonce, hashAlgorithm);
            
            return CompletableFuture.completedFuture(
                new UnmaskedPredicate(hash, reference, publicKey, algorithm, hashAlgorithm, nonce, tokenType)
            );
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }
    
    @Override
    public CompletableFuture<Boolean> isOwner(byte[] publicKey) {
        return CompletableFuture.completedFuture(Arrays.equals(getPublicKey(), publicKey));
    }

    @Override
    public CompletableFuture<Boolean> verify(Transaction<?> transaction) {
        try {
            // Step 1: Check if transaction has inclusion proof with authenticator
            if (transaction.getInclusionProof() == null || 
                transaction.getInclusionProof().getAuthenticator() == null) {
                return CompletableFuture.completedFuture(false);
            }
            
            Authenticator authenticator = transaction.getInclusionProof().getAuthenticator();
            
            // For now, we'll do basic verification
            // In a complete implementation, this would verify:
            // 1. Authenticator's signature matches this predicate's public key
            // 2. Transaction data integrity
            // 3. Inclusion proof validity
            
            // Step 1: Verify the authenticator's transaction hash
            DataHash txHash = authenticator.getTransactionHash();
            if (txHash == null) {
                return CompletableFuture.completedFuture(false);
            }
            
            // Step 2: Verify the authenticator signature
            return authenticator.verify(txHash)
                .thenCompose(dataHashValid -> {
                    if (!dataHashValid) {
                        return CompletableFuture.completedFuture(false);
                    }
                    
                    // Step 3: Create RequestId from public key and state hash
                    return RequestId.create(getPublicKey(), authenticator.getStateHash())
                        .thenCompose(requestId -> 
//                            transaction.getInclusionProof().verify(requestId)
//                                .thenApply(status -> status == InclusionProofVerificationStatus.OK)
                                null
                        );
                });
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("type", getType().name());
        root.put("publicKey", HexConverter.encode(getPublicKey()));
        root.put("algorithm", getAlgorithm());
        root.put("hashAlgorithm", getHashAlgorithm().name());
        root.put("nonce", HexConverter.encode(getNonce()));
        return root;
    }

    @Override
    public byte[] toCBOR() {
        // Encode as CBOR array: [type, publicKey, algorithm, hashAlgorithm, nonce]
        return CborEncoder.encodeArray(
            CborEncoder.encodeUnsignedInteger(getType().getValue()),
            CborEncoder.encodeByteString(getPublicKey()),
            CborEncoder.encodeTextString(getAlgorithm()),
            CborEncoder.encodeUnsignedInteger(getHashAlgorithm().getValue()),
            CborEncoder.encodeByteString(getNonce())
        );
    }
    
    /**
     * Calculate reference hash for unmasked predicate.
     */
    public static DataHash calculateReference(
            TokenType tokenType,
            String algorithm,
            byte[] publicKey,
            HashAlgorithm hashAlgorithm) {
        try {
            DataHasher hasher = new DataHasher(HashAlgorithm.SHA256);
            
            // Build CBOR array with predicate configuration
            byte[] cborArray = CborEncoder.encodeArray(
                CborEncoder.encodeTextString("UNMASKED"),
                tokenType.toCBOR(),
                CborEncoder.encodeTextString(algorithm),
                CborEncoder.encodeTextString(hashAlgorithm.name()),
                CborEncoder.encodeByteString(publicKey)
            );
            
            hasher.update(cborArray);
            return hasher.digest().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate reference", e);
        }
    }
    
    /**
     * Calculate hash for unmasked predicate.
     */
    private static DataHash calculateHash(
            DataHash reference,
            byte[] tokenId,
            byte[] nonce,
            HashAlgorithm hashAlgorithm) {
        try {
            DataHasher hasher = new DataHasher(HashAlgorithm.SHA256);
            
            // Build CBOR array with reference, tokenId, and nonce
            byte[] cborArray = CborEncoder.encodeArray(
                reference.toCBOR(),
                CborEncoder.encodeByteString(tokenId),
                CborEncoder.encodeByteString(nonce)
            );
            
            hasher.update(cborArray);
            return hasher.digest().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate hash", e);
        }
    }
    
    /**
     * Create an unmasked predicate from JSON data.
     * @param tokenId Token ID.
     * @param tokenType Token type.
     * @param jsonNode JSON node containing the unmasked predicate data.
     */
    public static CompletableFuture<UnmaskedPredicate> fromJSON(TokenId tokenId, TokenType tokenType, JsonNode jsonNode) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check if we need to extract from "data" field
                JsonNode dataNode = jsonNode.get("data");
                final JsonNode predicateNode = (dataNode != null && !dataNode.isNull()) ? dataNode : jsonNode;
                
                String publicKeyHex = predicateNode.get("publicKey").asText();
                String algorithm = predicateNode.get("algorithm").asText();
                String hashAlgorithmStr = predicateNode.get("hashAlgorithm").asText();
                String nonceHex = predicateNode.get("nonce").asText();
                
                byte[] publicKey = HexConverter.decode(publicKeyHex);
                byte[] nonce = HexConverter.decode(nonceHex);
                HashAlgorithm hashAlgorithm = HashAlgorithm.valueOf(hashAlgorithmStr);
                
                // Calculate reference and hash
                DataHash reference = calculateReference(tokenType, algorithm, publicKey, hashAlgorithm);
                DataHash hash = calculateHash(reference, tokenId.getBytes(), nonce, hashAlgorithm);
                
                return new UnmaskedPredicate(hash, reference, publicKey, algorithm, hashAlgorithm, nonce, tokenType);
            } catch (Exception e) {
                throw new RuntimeException("Failed to deserialize UnmaskedPredicate from JSON", e);
            }
        });
    }
}
