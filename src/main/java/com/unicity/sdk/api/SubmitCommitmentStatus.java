package com.unicity.sdk.api;

/**
 * Status codes for submit commitment response
 */
public enum SubmitCommitmentStatus {
    SUCCESS("SUCCESS"),
    FAILURE("FAILURE"),
    INVALID_REQUEST("INVALID_REQUEST");

    private final String value;

    SubmitCommitmentStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}