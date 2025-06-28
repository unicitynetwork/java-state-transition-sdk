package com.unicity.sdk.shared.jsonrpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JsonRpcResponse implements IJsonRpcResponse {
    private final String jsonrpc;
    private final Object result;
    private final JsonRpcError error;
    private final Object id;
    
    public JsonRpcResponse(
            @JsonProperty("jsonrpc") String jsonrpc,
            @JsonProperty("result") Object result,
            @JsonProperty("error") JsonRpcError error,
            @JsonProperty("id") Object id) {
        this.jsonrpc = jsonrpc;
        this.result = result;
        this.error = error;
        this.id = id;
    }
    
    @Override
    public String getJsonrpc() {
        return jsonrpc;
    }
    
    @Override
    public Object getResult() {
        return result;
    }
    
    @Override
    public JsonRpcError getError() {
        return error;
    }
    
    @Override
    public Object getId() {
        return id;
    }
}