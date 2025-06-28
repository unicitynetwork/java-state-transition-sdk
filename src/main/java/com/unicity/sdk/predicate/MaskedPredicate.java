package com.unicity.sdk.predicate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import com.unicity.sdk.identity.PublicKeyIdentity;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.signing.ISigningService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MaskedPredicate implements IPredicate {
    private final PublicKeyIdentity identity;
    private final DataHash hash;

    public MaskedPredicate(ISigningService signingService, HashAlgorithm algorithm) {
        this.identity = new PublicKeyIdentity(signingService.getPublicKey());
        this.hash = DataHasher.digest(algorithm, this.identity.toCBOR());
    }

    @Override
    public DataHash getHash() {
        return hash;
    }

    @Override
    public PublicKeyIdentity getReference() {
        return identity;
    }

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("type", PredicateType.MASKED.name());
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
            generator.writeString(PredicateType.MASKED.name());
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