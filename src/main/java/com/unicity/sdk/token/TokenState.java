package com.unicity.sdk.token;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.DataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.predicate.IPredicate;
import com.unicity.sdk.util.HexConverter;
import com.unicity.sdk.util.ByteArraySerializer;
import com.unicity.sdk.shared.cbor.CborEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Represents a snapshot of token ownership and associated data.
 */
public class TokenState implements ISerializable {
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
        DataHash hash = DataHasher.digest(HashAlgorithm.SHA256, baos.toByteArray());
        return new TokenState(unlockPredicate, data, hash);
    }

    public IPredicate getUnlockPredicate() {
        return unlockPredicate;
    }

    public byte[] getData() {
        return data;
    }

    public DataHash getHash() {
        return hash;
    }

    @Override
    public Object toJSON() {
        return this;
    }

    @Override
    public byte[] toCBOR() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            baos.write(CborEncoder.encodeArray(
                unlockPredicate.toCBOR(),
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
}