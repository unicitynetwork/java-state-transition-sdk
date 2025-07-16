
package com.unicity.sdk.jsonrpc;

public class JsonRpcNetworkError extends Exception {

  private final int status;
  private final String errorMessage;

  public JsonRpcNetworkError(int status, String message) {
    super(String.format("Network error [%s] occurred: %s", status, message));
    this.status = status;
    this.errorMessage = message;
  }

  public int getStatus() {
    return this.status;
  }

  public String getErrorMessage() {
    return this.errorMessage;
  }
}
