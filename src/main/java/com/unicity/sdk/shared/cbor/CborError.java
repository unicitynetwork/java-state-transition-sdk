
package com.unicity.sdk.shared.cbor;

public class CborError extends RuntimeException {
    public CborError(String message) {
        super(message);
    }
    
    public CborError(String message, Throwable cause) {
        super(message, cause);
    }
}
