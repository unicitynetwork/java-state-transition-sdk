package com.unicity.sdk.shared.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DataHasher {
    private final HashAlgorithm algorithm;
    private final MessageDigest messageDigest;
    
    public DataHasher(HashAlgorithm algorithm) {
        this.algorithm = algorithm;
        try {
            this.messageDigest = MessageDigest.getInstance(algorithm.getAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedHashAlgorithmError(algorithm);
        }
    }

    public HashAlgorithm getAlgorithm() {
        return algorithm;
    }

    public DataHasher update(byte[] data) {
        this.messageDigest.update(data);
        return this;
    }

    public DataHash digest() {
        return new DataHash(this.algorithm, this.messageDigest.digest());
    }
}