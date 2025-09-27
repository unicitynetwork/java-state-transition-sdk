package org.unicitylabs.sdk.jsonrpc;

/**
 * Exception thrown when an API request is unauthorized (HTTP 401).
 * This typically occurs when an API key is missing or invalid.
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}