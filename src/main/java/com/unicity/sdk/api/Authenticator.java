package com.unicity.sdk.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.cbor.CborDecoder;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.cbor.CustomCborDecoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.signing.ISignature;
import com.unicity.sdk.shared.signing.ISigningService;
import com.unicity.sdk.shared.signing.SigningService;
import com.unicity.sdk.shared.signing.Signature;
import com.unicity.sdk.shared.hash.JavaDataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.util.HexConverter;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Authenticator for transaction submission
 */
public class Authenticator implements ISerializable {
    private final ISignature signature;
    private final DataHash transactionHash;
    private final DataHash stateHash;
    private final byte[] address;

    private Authenticator(ISignature signature, DataHash transactionHash, DataHash stateHash, byte[] address) {
        this.signature = signature;
        this.transactionHash = transactionHash;
        this.stateHash = stateHash;
        this.address = address;
    }

    public static CompletableFuture<Authenticator> create(
            ISigningService<?> signingService,
            DataHash transactionHash,
            DataHash stateHash) {
        
        // TypeScript SDK signs only the transactionHash, not the concatenation
        return signingService.sign(transactionHash)
                .thenApply(signature -> new Authenticator(signature, transactionHash, stateHash, signingService.getPublicKey()));
    }

    public ISignature getSignature() {
        return signature;
    }

    public DataHash getTransactionHash() {
        return transactionHash;
    }

    public DataHash getStateHash() {
        return stateHash;
    }
    
    public byte[] getPublicKey() {
        return address;
    }
    
    /**
     * Factory method for Jackson deserialization from JSON.
     * Matches TypeScript SDK's IAuthenticatorJson interface.
     */
    @JsonCreator
    public static Authenticator fromJson(
            @JsonProperty("algorithm") String algorithm,
            @JsonProperty("publicKey") String publicKeyHex,
            @JsonProperty("signature") String signatureHex,
            @JsonProperty("stateHash") String stateHashHex) {
        
        // For now, we'll create a dummy Authenticator for deserialization
        // In a real implementation, we'd need to properly reconstruct all fields
        byte[] publicKey = HexConverter.decode(publicKeyHex);
        byte[] fullSignatureBytes = HexConverter.decode(signatureHex);
        DataHash stateHash = DataHash.fromImprint(HexConverter.decode(stateHashHex));
        
        // Extract signature and recovery byte (last byte is recovery)
        byte[] signatureBytes = Arrays.copyOfRange(fullSignatureBytes, 0, fullSignatureBytes.length - 1);
        int recovery = fullSignatureBytes[fullSignatureBytes.length - 1] & 0xFF;
        
        // Create signature from bytes
        ISignature signature = new Signature(signatureBytes, recovery);
        
        // Note: We don't have transactionHash in the JSON, which is a problem
        // For now, use null which will cause issues if verify() is called
        return new Authenticator(signature, null, stateHash, publicKey);
    }
    
    /**
     * Create Authenticator from JSON object.
     */
    public static Authenticator fromJSON(Object jsonObj) {
        if (jsonObj instanceof java.util.Map) {
            java.util.Map<String, Object> map = (java.util.Map<String, Object>) jsonObj;
            return fromJson(
                (String) map.get("algorithm"),
                (String) map.get("publicKey"),
                (String) map.get("signature"),
                (String) map.get("stateHash")
            );
        }
        throw new IllegalArgumentException("Invalid Authenticator JSON format");
    }

    public CompletableFuture<Boolean> verify(DataHash hash) {
        // When deserialized from JSON, we don't have transactionHash
        // In this case, verify the signature against the provided hash
        DataHash hashToVerify = transactionHash != null ? transactionHash : hash;
        
        // Verify that the provided hash matches our transaction hash (if we have one)
        if (transactionHash != null && !hash.equals(transactionHash)) {
            return CompletableFuture.completedFuture(false);
        }
        
        // Verify signature against the hash
        return SigningService.verifyWithPublicKey(
            hashToVerify,
            signature.getBytes(),
            address
        );
    }

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        
        root.put("algorithm", "secp256k1");
        root.put("publicKey", HexConverter.encode(address));
        root.put("signature", signature.toJSON().toString());
        root.put("stateHash", stateHash.toJSON().toString());
        
        return root;
    }

    @Override
    public byte[] toCBOR() {
        return CborEncoder.encodeArray(
            CborEncoder.encodeTextString("secp256k1"),  // algorithm
            CborEncoder.encodeByteString(address),   // publicKey
            CborEncoder.encodeByteString(((Signature)signature).encode()),  // signature bytes
            CborEncoder.encodeByteString(stateHash.getImprint())  // stateHash imprint
        );
    }
    
    /**
     * Deserialize Authenticator from CBOR.
     * @param cbor The CBOR-encoded bytes
     * @return An Authenticator instance
     */
    public static Authenticator fromCBOR(byte[] cbor) {
        try {
            CustomCborDecoder.DecodeResult result = CustomCborDecoder.decode(cbor, 0);
            if (!(result.value instanceof List)) {
                throw new RuntimeException("Expected array for Authenticator");
            }
            
            List<?> array = (List<?>) result.value;
            if (array.size() < 3) {
                throw new RuntimeException("Invalid Authenticator array size");
            }
            
            // Deserialize signature
            Signature signature = Signature.fromCBOR((byte[]) array.get(0));
            
            // Deserialize transaction hash
            DataHash transactionHash = DataHash.fromCBOR((byte[]) array.get(1));
            
            // Deserialize state hash
            DataHash stateHash = DataHash.fromCBOR((byte[]) array.get(2));
            
            // Note: address is not stored in CBOR format, it needs to be recovered
            // from signature if needed
            return new Authenticator(signature, transactionHash, stateHash, new byte[0]);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize Authenticator from CBOR", e);
        }
    }
}