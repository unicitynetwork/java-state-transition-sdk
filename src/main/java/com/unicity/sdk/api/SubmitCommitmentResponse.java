package com.unicity.sdk.api;

/**
 * Response from submit commitment request
 */
public class SubmitCommitmentResponse {
    private final SubmitCommitmentStatus status;

    public SubmitCommitmentResponse(SubmitCommitmentStatus status) {
        this.status = status;
    }

    public SubmitCommitmentStatus getStatus() {
        return status;
    }
}