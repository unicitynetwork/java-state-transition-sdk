package com.unicity.sdk.shared.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.CompletableFuture;

public class JavaDataHasher implements IDataHasher {
    private final HashAlgorithm algorithm;
    private final MessageDigest messageDigest;
    
    public JavaDataHasher(HashAlgorithm algorithm) {
        this.algorithm = algorithm;
        try {
            this.messageDigest = MessageDigest.getInstance(getAlgorithmName(algorithm));
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedHashAlgorithmError(algorithm);
        }
    }
    
    @Override
    public HashAlgorithm getAlgorithm() {
        return algorithm;
    }
    
    @Override
    public IDataHasher update(byte[] data) {
        messageDigest.update(data);
        return this;
    }
    
    @Override
    public CompletableFuture<DataHash> digest() {
        byte[] hash = messageDigest.digest();
        return CompletableFuture.completedFuture(new DataHash(hash, algorithm));
    }
    
    private static String getAlgorithmName(HashAlgorithm algorithm) {
        switch (algorithm) {
            case SHA256:
                return "SHA-256";
            case SHA224:
                return "SHA-224";
            case SHA384:
                return "SHA-384";
            case SHA512:
                return "SHA-512";
            case RIPEMD160:
                return "RIPEMD160";
            default:
                throw new UnsupportedHashAlgorithmError(algorithm);
        }
    }
}