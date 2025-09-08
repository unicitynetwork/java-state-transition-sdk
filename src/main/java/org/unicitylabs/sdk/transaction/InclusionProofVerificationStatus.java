package org.unicitylabs.sdk.transaction;

/**
 * Status codes for verifying an InclusionProof.
 */
public enum InclusionProofVerificationStatus {
    NOT_AUTHENTICATED("NOT_AUTHENTICATED"),
    PATH_NOT_INCLUDED("PATH_NOT_INCLUDED"),
    PATH_INVALID("PATH_INVALID"),
    OK("OK");

    private final String value;

    InclusionProofVerificationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}