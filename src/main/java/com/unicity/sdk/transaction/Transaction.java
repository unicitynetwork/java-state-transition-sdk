
package com.unicity.sdk.transaction;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import com.unicity.sdk.ISerializable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class Transaction implements ISerializable {
    private final ISerializable data;
    private final InclusionProof inclusionProof;

    public Transaction(ISerializable data, InclusionProof inclusionProof) {
        this.data = data;
        this.inclusionProof = inclusionProof;
    }

    public ISerializable getData() {
        return data;
    }

    public InclusionProof getInclusionProof() {
        return inclusionProof;
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
