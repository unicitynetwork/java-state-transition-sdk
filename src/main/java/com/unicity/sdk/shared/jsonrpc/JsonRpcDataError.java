
package com.unicity.sdk.shared.jsonrpc;

public class JsonRpcDataError extends Error {
    private final int code;
    private final String message;
    private final Object data;

    public JsonRpcDataError(int code, String message, Object data) {
        super(message);
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
