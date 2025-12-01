package org.unicitylabs.sdk.utils.helpers;

import org.unicitylabs.sdk.api.StateId;
import org.unicitylabs.sdk.transaction.InclusionProofVerificationStatus;

public class CommitmentResult {
    private final String userName;
    private final String threadName;
    private final StateId stateId;
    private final boolean success;
    private final long startTime;
    private final long endTime;

    public boolean verified;
    private long inclusionStart;
    private long inclusionEnd;
    private String status;

    public CommitmentResult(String userName, String threadName, StateId stateId,
                            boolean success, long startTime, long endTime) {
        this.userName = userName;
        this.threadName = threadName;
        this.stateId = stateId;
        this.success = success;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public boolean isSuccess() { return success; }

    public void markVerified(long start, long end) {
        this.verified = true;
        this.inclusionStart = start;
        this.inclusionEnd = end;
        this.status = InclusionProofVerificationStatus.OK.toString();
    }

    public StateId getStateId() {
        return this.stateId;
    }

    public void markFailedVerification(long start, long end, String status) {
        this.verified = false;
        this.inclusionStart = start;
        this.inclusionEnd = end;
        this.status = status.toString();
    }

    public boolean isVerified() {
        return this.verified;
    }

    public String getStatus(){
        return this.status;
    }

    // Add these getter methods for the multi-aggregator functionality
    public String getUserName() {
        return this.userName;
    }

    public String getThreadName() {
        return this.threadName;
    }

    // Helper method to get inclusion proof verification duration
    public long getInclusionDurationNanos() {
        return this.inclusionEnd - this.inclusionStart;
    }

    public double getInclusionDurationMillis() {
        return getInclusionDurationNanos() / 1_000_000.0;
    }

}
