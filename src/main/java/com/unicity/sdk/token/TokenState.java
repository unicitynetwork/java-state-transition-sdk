package com.unicity.sdk.token;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.unicity.sdk.Hashable;
import com.unicity.sdk.predicate.IPredicate;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.shared.util.HexConverter;
import com.unicity.sdk.util.ByteArraySerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Represents a snapshot of token ownership and associated data.
 */
public class TokenState implements Hashable {
    @JsonProperty("unlockPredicate")
    private final IPredicate unlockPredicate;

    @JsonSerialize(using = ByteArraySerializer.class)
    private final byte[] data;

    private final DataHash hash;

    private TokenState(IPredicate unlockPredicate, byte[] data, DataHash hash) {
        this.unlockPredicate = unlockPredicate;
        this.data = data;
        this.hash = hash;
    }

    public static TokenState create(IPredicate unlockPredicate, byte[] data) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(unlockPredicate.getHash().toCBOR());
            baos.write(data != null ? data : new byte[0]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        DataHash hash = new DataHasher(HashAlgorithm.SHA256).update(baos.toByteArray()).digest();
        return new TokenState(unlockPredicate, data, hash);
    }

    public IPredicate getUnlockPredicate() {
        return this.unlockPredicate;
    }

    public byte[] getData() {
        return this.data;
    }

    public DataHash getHash() {
        return this.hash;
    }

    public Object toJSON() {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("data", HexConverter.encode(data != null ? data : new byte[0]));
//        root.set("unlockPredicate", mapper.valueToTree(unlockPredicate.toJSON()));
        return root;
    }

    public byte[] toCBOR() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(CborEncoder.encodeArray(
//                unlockPredicate.toCBOR(),
                CborEncoder.encodeByteString(data != null ? data : new byte[0])
            ));
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode TokenState", e);
        }
        return baos.toByteArray();
    }

    @Override
    public String toString() {
        return "TokenState:" +
                "\n  " + unlockPredicate.toString() +
                "\n  Data: " + (data != null ? HexConverter.encode(data) : "null") +
                "\n  Hash: " + hash.toString();
    }
    
    /**
     * Deserialize TokenState from JSON.
     * @param jsonNode JSON node containing token state
     * @return TokenState instance
     */
    public static TokenState fromJSON(JsonNode jsonNode) throws Exception {
        // Deserialize predicate
        JsonNode predicateNode = jsonNode.get("unlockPredicate");
        
        // Get data if present
        byte[] data = null;
        if (jsonNode.has("data") && !jsonNode.get("data").isNull()) {
            String dataHex = jsonNode.get("data").asText();
            data = HexConverter.decode(dataHex);
        }
        
        return create(null, data);
    }
}