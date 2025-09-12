package org.unicitylabs.sdk.utils.helpers;

import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.transaction.InclusionProofVerificationStatus;

public class CommitmentResult {
    private final String userName;
    private final String threadName;
    private final RequestId requestId;
    private final boolean success;
    private final long startTime;
    private final long endTime;

    public boolean verified;
    private long inclusionStart;
    private long inclusionEnd;
    private String status;

    public CommitmentResult(String userName, String threadName, RequestId requestId,
                            boolean success, long startTime, long endTime) {
        this.userName = userName;
        this.threadName = threadName;
        this.requestId = requestId;
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

    public RequestId getRequestId() {
        return this.requestId;
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

}
