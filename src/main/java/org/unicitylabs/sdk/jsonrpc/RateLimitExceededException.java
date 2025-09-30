package org.unicitylabs.sdk.jsonrpc;

import org.jetbrains.annotations.NotNull;

/**
 * Exception thrown when a rate limit is exceeded in the API.
 * <p>
 * The {@code retryAfterSeconds} field indicates the number of seconds
 * the client should wait before retrying the request.
 */
public class RateLimitExceededException extends RuntimeException {
    
    private final int retryAfterSeconds;
    
    public RateLimitExceededException(int retryAfterSeconds) {
        super(getMessage(retryAfterSeconds));
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public RateLimitExceededException(int retryAfterSeconds, Throwable cause) {
        super(getMessage(retryAfterSeconds), cause);
        this.retryAfterSeconds = retryAfterSeconds;
    }
    
    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }

    @NotNull
    private static String getMessage(int retryAfterSeconds) {
        return "Rate limit exceeded. Please retry after " + retryAfterSeconds + " seconds";
    }
}