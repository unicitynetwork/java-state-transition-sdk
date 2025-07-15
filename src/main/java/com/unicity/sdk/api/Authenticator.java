package com.unicity.sdk.api;

import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.signing.Signature;
import com.unicity.sdk.shared.signing.SigningService;

import java.util.Arrays;
import java.util.Objects;

/**
 * Authenticator for transaction submission
 */
public class Authenticator {
    private final String algorithm;
    private final Signature signature;
    private final DataHash stateHash;
    private final byte[] publicKey;

    public Authenticator(String algorithm, byte[] publicKey, Signature signature, DataHash stateHash) {
        this.algorithm = algorithm;
        this.publicKey = publicKey;
        this.signature = signature;
        this.stateHash = stateHash;
    }

    public static Authenticator create(
            SigningService signingService,
            DataHash transactionHash,
            DataHash stateHash) {

        return new Authenticator(signingService.getAlgorithm(), signingService.getPublicKey(), signingService.sign(transactionHash), stateHash);
    }

    public Signature getSignature() {
        return this.signature;
    }

    public String getAlgorithm() {
        return this.algorithm;
    }

    public DataHash getStateHash() {
        return this.stateHash;
    }
    
    public byte[] getPublicKey() {
        return this.publicKey;
    }

    public boolean verify(DataHash hash) {
        return SigningService.verifyWithPublicKey(hash, this.signature.getBytes(), this.publicKey);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Authenticator)) return false;
        Authenticator that = (Authenticator) o;
        return Objects.equals(algorithm, that.algorithm) && Objects.equals(signature, that.signature) && Objects.equals(stateHash, that.stateHash) && Objects.deepEquals(publicKey, that.publicKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(algorithm, signature, stateHash, Arrays.hashCode(publicKey));
    }
}