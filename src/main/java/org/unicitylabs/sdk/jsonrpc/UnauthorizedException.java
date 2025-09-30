package org.unicitylabs.sdk.jsonrpc;

/**
 * Exception thrown when an API request is unauthorized (HTTP 401).
 * This typically occurs when an API key is missing or invalid.
 */
public class UnauthorizedException extends RuntimeException {

    public static final String MESSAGE = "Unauthorized: Invalid or missing API key";

    public UnauthorizedException() {
        super(MESSAGE);
    }
    
    public UnauthorizedException(Throwable cause) {
        super(MESSAGE, cause);
    }
}