package org.unicitylabs.sdk.jsonrpc;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class JsonRpcRequest {

  private final UUID id;
  private final String method;
  private final Object params;

  public JsonRpcRequest(String method, Object params) {
    this(UUID.randomUUID(), method, params);
  }

  @JsonCreator
  public JsonRpcRequest(
      @JsonProperty("id") UUID id,
      @JsonProperty("method") String method,
      @JsonProperty("params") Object params
  ) {
    this.id = id;
    this.method = method;
    this.params = params;
  }

  @JsonGetter("id")
  public UUID getId() {
    return this.id;
  }

  @JsonGetter("jsonrpc")
  public String getVersion() {
    return "2.0";
  }

  @JsonGetter("method")
  public String getMethod() {
    return this.method;
  }

  @JsonGetter("params")
  public Object getParams() {
    return this.params;
  }
}
