
package com.unicity.sdk.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class Transaction<T extends ISerializable> implements ISerializable {
    private final T data;
    private final InclusionProof inclusionProof;

    public Transaction(T data, InclusionProof inclusionProof) {
        this.data = data;
        this.inclusionProof = inclusionProof;
    }

    public T getData() {
        return data;
    }

    public InclusionProof getInclusionProof() {
        return inclusionProof;
    }

    public boolean containsData(byte[] stateData) {
        if (!(data instanceof TransactionData)) {
            return CompletableFuture.completedFuture(false);
        }
        
        TransactionData txData = (TransactionData) data;
        
        // If transaction has no data hash and state data is empty, they match
        if (txData.getDataHash() == null && (stateData == null || stateData.length == 0)) {
            return CompletableFuture.completedFuture(true);
        }
        
        // If one is null but not the other, they don't match
        if (txData.getDataHash() == null || stateData == null) {
            return CompletableFuture.completedFuture(false);
        }
        
        DataHasher hasher = new DataHasher(HashAlgorithm.SHA256);
        hasher.update(stateData);
        
        return hasher.digest().equals(txData.getDataHash());
    }

    @Override
    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.set("data", mapper.valueToTree(data.toJSON()));
        root.set("inclusionProof", mapper.valueToTree(inclusionProof.toJSON()));
        return root;
    }

    @Override
    public byte[] toCBOR() {
        CBORFactory factory = new CBORFactory();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             CBORGenerator generator = factory.createGenerator(baos)) {
            generator.writeStartObject();
            generator.writeFieldName("data");
            generator.writeBinary(data.toCBOR());
            generator.writeFieldName("inclusionProof");
            generator.writeBinary(inclusionProof.toCBOR());
            generator.writeEndObject();
            generator.flush();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
}
