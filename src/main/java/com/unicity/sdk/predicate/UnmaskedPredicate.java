
package com.unicity.sdk.predicate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import com.unicity.sdk.identity.PublicKeyIdentity;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.hash.JavaDataHasher;
import com.unicity.sdk.transaction.Transaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class UnmaskedPredicate implements IPredicate {
    private final PublicKeyIdentity identity;
    private final DataHash hash;
    private final DataHash reference;

    public UnmaskedPredicate(PublicKeyIdentity identity, HashAlgorithm algorithm) {
        this.identity = identity;
        this.hash = DataHasher.digest(algorithm, identity.toCBOR());
        // Calculate reference using SHA256 hash of public key
        // In a full implementation, this would include more predicate configuration
        try {
            JavaDataHasher hasher = new JavaDataHasher(HashAlgorithm.SHA256);
            hasher.update(identity.getPublicKey());
            this.reference = hasher.digest().get();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate reference", e);
        }
    }

    @Override
    public DataHash getHash() {
        return hash;
    }

    @Override
    public DataHash getReference() {
        return reference;
    }

    @Override
    public CompletableFuture<Boolean> isOwner(byte[] publicKey) {
        // TODO: Implement ownership check based on identity
        return CompletableFuture.completedFuture(false);
    }

    @Override
    public CompletableFuture<Boolean> verify(Transaction<?> transaction) {
        // TODO: Implement verification
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("type", PredicateType.UNMASKED.name());
        root.put("identity", identity.toJSON().toString());
        return root;
    }

    @Override
    public byte[] toCBOR() {
        CBORFactory factory = new CBORFactory();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             CBORGenerator generator = factory.createGenerator(baos)) {
            generator.writeStartObject();
            generator.writeFieldName("type");
            generator.writeString(PredicateType.UNMASKED.name());
            generator.writeFieldName("identity");
            generator.writeBinary(identity.toCBOR());
            generator.writeEndObject();
            generator.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
