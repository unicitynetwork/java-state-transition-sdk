package com.unicity.sdk.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.signing.ISignature;
import com.unicity.sdk.shared.signing.ISigningService;

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

    public CompletableFuture<Boolean> verify(DataHash hash) {
        // TODO: Implement verification logic
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        
        root.put("publicKey", com.unicity.sdk.shared.util.HexConverter.encode(address));
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