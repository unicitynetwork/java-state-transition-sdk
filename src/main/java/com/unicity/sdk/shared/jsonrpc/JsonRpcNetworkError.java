
package com.unicity.sdk.shared.jsonrpc;

public class JsonRpcNetworkError extends Exception {
    private final int status;
    private final String responseText;
    
    public JsonRpcNetworkError(int status, String responseText) {
        super("JSON-RPC network error: " + status + " - " + responseText);
        this.status = status;
        this.responseText = responseText;
    }
    
    public int getStatus() {
        return status;
    }
    
    public String getResponseText() {
        return responseText;
    }
}
