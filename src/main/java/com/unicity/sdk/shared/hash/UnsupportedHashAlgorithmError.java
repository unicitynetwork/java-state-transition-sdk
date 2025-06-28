
package com.unicity.sdk.shared.hash;

public class UnsupportedHashAlgorithmError extends RuntimeException {
    private final HashAlgorithm algorithm;

    public UnsupportedHashAlgorithmError(String message) {
        super(message);
        this.algorithm = null;
    }
    
    public UnsupportedHashAlgorithmError(HashAlgorithm algorithm) {
        super("Unsupported hash algorithm: " + algorithm);
        this.algorithm = algorithm;
    }

    public HashAlgorithm getAlgorithm() {
        return algorithm;
    }
}
