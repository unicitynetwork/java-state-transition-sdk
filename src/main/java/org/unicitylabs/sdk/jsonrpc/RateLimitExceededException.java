package org.unicitylabs.sdk.jsonrpc;

public class RateLimitExceededException extends RuntimeException {
    
    private final int retryAfterSeconds;
    
    public RateLimitExceededException(String message, int retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    public RateLimitExceededException(String message, int retryAfterSeconds, Throwable cause) {
        super(message, cause);
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}