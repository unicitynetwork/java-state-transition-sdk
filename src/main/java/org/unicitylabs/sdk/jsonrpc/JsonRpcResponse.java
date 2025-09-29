package org.unicitylabs.sdk.jsonrpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;
import java.util.UUID;

public class JsonRpcResponse<T> {

  private final String version;
  private final T result;
  private final JsonRpcError error;
  private final UUID id;

  @JsonCreator
  JsonRpcResponse(
      @JsonProperty("jsonrpc") String version,
      @JsonProperty("result") T result,
      @JsonProperty("error") JsonRpcError error,
      @JsonProperty("id") UUID id
  ) {
    this.version = version;
    this.result = result;
    this.error = error;
    this.id = id;
  }

  @JsonGetter("jsonrpc")
  public String getVersion() {
    return this.version;
  }

  @JsonGetter("result")
  public T getResult() {
    return this.result;
  }

  @JsonGetter("error")
  public JsonRpcError getError() {
    return this.error;
  }

  @JsonGetter("id")
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