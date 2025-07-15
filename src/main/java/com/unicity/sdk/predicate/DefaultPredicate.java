package com.unicity.sdk.predicate;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.shared.hash.DataHash;
import com.unicity.sdk.shared.hash.HashAlgorithm;
import com.unicity.sdk.transaction.InclusionProofVerificationStatus;
import com.unicity.sdk.transaction.Transaction;

import java.util.Arrays;

/**
 * Base class for predicates, matching TypeScript implementation
 */
public abstract class DefaultPredicate implements IPredicate {
    private final PredicateType type;
    private final byte[] publicKey;
    private final String algorithm;
    private final HashAlgorithm hashAlgorithm;
    private final byte[] nonce;
    private final DataHash reference;
    private final DataHash hash;

    protected DefaultPredicate(
            PredicateType type,
            byte[] publicKey,
            String algorithm,
            HashAlgorithm hashAlgorithm,
            byte[] nonce,
            DataHash reference,
            DataHash hash) {
        this.type = type;
        this.publicKey = Arrays.copyOf(publicKey, publicKey.length);
        this.algorithm = algorithm;
        this.hashAlgorithm = hashAlgorithm;
        this.nonce = Arrays.copyOf(nonce, nonce.length);
        this.reference = reference;
        this.hash = hash;
    }

    @JsonProperty
    public String getType() {
        return this.type.getType();
    }

    @JsonProperty
    public byte[] getPublicKey() {
        return Arrays.copyOf(this.publicKey, this.publicKey.length);
    }

    @JsonProperty
    public String getAlgorithm() {
        return this.algorithm;
    }

    @JsonProperty
    public HashAlgorithm getHashAlgorithm() {
        return this.hashAlgorithm;
    }

    @JsonProperty
    public byte[] getNonce() {
        return Arrays.copyOf(this.nonce, this.nonce.length);
    }

    @Override
    public DataHash getHash() {
        return this.hash;
    }

    @Override
    public DataHash getReference() {
        return this.reference;
    }

    @Override
    public boolean isOwner(byte[] publicKey) {
        return Arrays.equals(this.publicKey, publicKey);
    }

    public boolean verify(Transaction<?> transaction) {
        Authenticator authenticator = transaction.getInclusionProof().getAuthenticator();
        DataHash transactionHash = transaction.getInclusionProof().getTransactionHash();

        if (authenticator == null || transactionHash == null) {
            return false;
        }

        if (!Arrays.equals(authenticator.getPublicKey(), this.publicKey)) {
            return false;
        }

        if (!authenticator.verify(transaction.getData().getHash())) {
            return false;
        }

        RequestId requestId = RequestId.create(this.publicKey, transaction.getData().getSourceState().getHash());
        return transaction.getInclusionProof().verify(requestId) == InclusionProofVerificationStatus.OK;
    }
}