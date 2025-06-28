
package com.unicity.sdk.shared.jsonrpc;

public interface IJsonRpcResponse {
    String getJsonrpc();
    Object getResult();
    JsonRpcDataError getError();
    int getId();
}
