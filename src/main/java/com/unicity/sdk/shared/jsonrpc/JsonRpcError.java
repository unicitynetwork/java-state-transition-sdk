package com.unicity.sdk.shared.jsonrpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JsonRpcError {
    private final int code;
    private final String message;
    
    public JsonRpcError(@JsonProperty("code") int code, @JsonProperty("message") String message) {
        this.code = code;
        this.message = message;
    }
    
    public int getCode() {
        return code;
    }
    
    public String getMessage() {
        return message;
    }
}