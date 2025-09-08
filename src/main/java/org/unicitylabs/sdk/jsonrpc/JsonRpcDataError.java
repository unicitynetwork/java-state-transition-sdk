
package org.unicitylabs.sdk.jsonrpc;

public class JsonRpcDataError extends Exception {

  private final JsonRpcError error;

  public JsonRpcDataError(JsonRpcError error) {
    super(error.getMessage());
    this.error = error;
  }

  public int getCode() {
    return error.getCode();
  }

  public JsonRpcError getError() {
    return error;
  }
}
