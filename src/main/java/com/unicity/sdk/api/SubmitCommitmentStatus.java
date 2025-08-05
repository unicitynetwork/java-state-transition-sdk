package com.unicity.sdk.api;

/**
 * Status codes for submit commitment response.
 */
public enum SubmitCommitmentStatus {
    /** The commitment was accepted and stored. */
    SUCCESS("SUCCESS"),
    /** Signature verification failed. */
    AUTHENTICATOR_VERIFICATION_FAILED("AUTHENTICATOR_VERIFICATION_FAILED"),
    /** Request identifier did not match the payload. */
    REQUEST_ID_MISMATCH("REQUEST_ID_MISMATCH"),
    /** A commitment with the same request id already exists. */
    REQUEST_ID_EXISTS("REQUEST_ID_EXISTS");

    private final String value;

    SubmitCommitmentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    
    public static SubmitCommitmentStatus fromString(String value) {
        for (SubmitCommitmentStatus status : SubmitCommitmentStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}