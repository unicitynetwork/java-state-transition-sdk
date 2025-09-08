package org.unicitylabs.sdk.jsonrpc;

public class JsonRpcError {

  private final int code;
  private final String message;

  public JsonRpcError(int code, String message) {
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