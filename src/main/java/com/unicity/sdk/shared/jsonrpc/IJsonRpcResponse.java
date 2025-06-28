
package com.unicity.sdk.shared.jsonrpc;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON-RPC response.
 */
public interface IJsonRpcResponse {
    /**
     * JSON-RPC version.
     */
    @JsonProperty("jsonrpc")
    String getJsonrpc();
    
    /**
     * Result data.
     */
    @JsonProperty("result")
    Object getResult();
    
    /**
     * Error data.
     */
    @JsonProperty("error")
    JsonRpcError getError();
    
    /**
     * Request ID.
     */
    @JsonProperty("id")
    Object getId(); // Can be String, Number, or null
}
