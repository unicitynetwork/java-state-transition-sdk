package com.unicity.sdk.shared.hash;

import java.security.MessageDigest;

public class DataHasher {
    public static DataHash digest(HashAlgorithm algorithm, byte[] data) {
        try {
            String algorithmName = getAlgorithmName(algorithm);
            MessageDigest digest = MessageDigest.getInstance(algorithmName);
            return new DataHash(digest.digest(data), algorithm);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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