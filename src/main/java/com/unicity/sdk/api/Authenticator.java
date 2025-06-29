package com.unicity.sdk.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.signing.ISignature;
import com.unicity.sdk.shared.signing.ISigningService;
import com.unicity.sdk.shared.signing.SigningService;
import com.unicity.sdk.shared.hash.JavaDataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.util.HexConverter;

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
        
        byte[] message = new byte[transactionHash.getHash().length + stateHash.getHash().length];
        System.arraycopy(transactionHash.getHash(), 0, message, 0, transactionHash.getHash().length);
        System.arraycopy(stateHash.getHash(), 0, message, transactionHash.getHash().length, stateHash.getHash().length);
        
        return signingService.sign(message)
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

    public CompletableFuture<Boolean> verify(DataHash hash) {
        // Verify that the provided hash matches our transaction hash
        if (!hash.equals(transactionHash)) {
            return CompletableFuture.completedFuture(false);
        }
        
        // Verify the signature
        byte[] message = new byte[transactionHash.getHash().length + stateHash.getHash().length];
        System.arraycopy(transactionHash.getHash(), 0, message, 0, transactionHash.getHash().length);
        System.arraycopy(stateHash.getHash(), 0, message, transactionHash.getHash().length, stateHash.getHash().length);
        
        // Create a hash of the message
        JavaDataHasher hasher = new JavaDataHasher(HashAlgorithm.SHA256);
        hasher.update(message);
        
        return hasher.digest().thenCompose(messageHash -> 
            SigningService.verifyWithPublicKey(
                messageHash,
                signature.getBytes(),
                address
            )
        );
    }

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        
        root.put("publicKey", HexConverter.encode(address));
        root.put("signature", signature.toJSON().toString());
        
        return root;
    }

    @Override
    public byte[] toCBOR() {
        return CborEncoder.encodeArray(
            signature.toCBOR(),
            transactionHash.toCBOR(),
            stateHash.toCBOR()
        );
    }
}