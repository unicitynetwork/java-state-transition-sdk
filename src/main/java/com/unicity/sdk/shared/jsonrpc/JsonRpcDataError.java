
package com.unicity.sdk.shared.jsonrpc;

public class JsonRpcDataError extends Exception {
    private final JsonRpcError error;

    public JsonRpcDataError(JsonRpcError error) {
        super(error.getMessage());
        this.error = error;
    }

    public int getCode() {
        return error.getCode();
    }

    @Override
    public String getMessage() {
        return error.getMessage();
    }

    public JsonRpcError getError() {
        return error;
    }
}
