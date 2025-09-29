package org.unicitylabs.sdk.jsonrpc;

/**
 * Exception thrown when a rate limit is exceeded in the API.
 * <p>
 * The {@code retryAfterSeconds} field indicates the number of seconds
 * the client should wait before retrying the request.
 */
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