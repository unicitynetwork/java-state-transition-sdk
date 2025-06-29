package com.unicity.sdk.api;

import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.JavaDataHasher;
import com.unicity.sdk.shared.hash.HashAlgorithm;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Request ID for tracking transactions
 */
public class RequestId implements ISerializable {
    private final byte[] publicKey;
    private final DataHash stateHash;
    private final BigInteger bigIntValue;

    private RequestId(byte[] publicKey, DataHash stateHash, BigInteger bigIntValue) {
        this.publicKey = Arrays.copyOf(publicKey, publicKey.length);
        this.stateHash = stateHash;
        this.bigIntValue = bigIntValue;
    }

    public static CompletableFuture<RequestId> create(byte[] publicKey, DataHash stateHash) {
        JavaDataHasher hasher = new JavaDataHasher(HashAlgorithm.SHA256);
        hasher.update(publicKey);
        hasher.update(stateHash.toCBOR());
        
        return hasher.digest().thenApply(hash -> {
            byte[] hashBytes = hash.getHash();
            // Take first 8 bytes for BigInteger
            byte[] bigIntBytes = Arrays.copyOfRange(hashBytes, 0, Math.min(8, hashBytes.length));
            BigInteger bigIntValue = new BigInteger(1, bigIntBytes);
            
            return new RequestId(publicKey, stateHash, bigIntValue);
        });
    }

    public byte[] getPublicKey() {
        return Arrays.copyOf(publicKey, publicKey.length);
    }

    public DataHash getStateHash() {
        return stateHash;
    }

    public BigInteger toBigInt() {
        return bigIntValue;
    }

    @Override
    public Object toJSON() {
        // Convert BigInteger to hex
        byte[] bytes = bigIntValue.toByteArray();
        // Remove leading zero byte if present (from BigInteger's two's complement representation)
        if (bytes.length > 1 && bytes[0] == 0) {
            bytes = Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        return com.unicity.sdk.shared.util.HexConverter.encode(bytes);
    }

    @Override
    public byte[] toCBOR() {
        return CborEncoder.encodeArray(
            CborEncoder.encodeByteString(publicKey),
            stateHash.toCBOR()
        );
    }
}