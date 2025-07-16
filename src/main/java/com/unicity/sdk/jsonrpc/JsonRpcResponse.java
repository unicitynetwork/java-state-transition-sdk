package com.unicity.sdk.jsonrpc;

import java.util.Objects;
import java.util.UUID;

public class JsonRpcResponse<T> {

  private final String version;
  private final T result;
  private final JsonRpcError error;
  private final UUID id;

  public JsonRpcResponse(
      String version,
      T result,
      JsonRpcError error,
      UUID id) {
    this.version = version;
    this.result = result;
    this.error = error;
    this.id = id;
  }

  public String getVersion() {
    return this.version;
  }

  public T getResult() {
    return this.result;
  }

  public JsonRpcError getError() {
    return this.error;
  }

  public UUID getId() {
    return this.id;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof JsonRpcResponse)) {
      return false;
    }
    JsonRpcResponse<?> that = (JsonRpcResponse<?>) o;
    return Objects.equals(this.version, that.version) && Objects.equals(this.result,
        that.result) && Objects.equals(this.error, that.error) && Objects.equals(this.id,
        that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.version, this.result, this.error, this.id);
  }
}