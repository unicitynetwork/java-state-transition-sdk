package com.unicity.sdk.jsonrpc;

import java.util.UUID;

public class JsonRpcRequest {

  private final UUID id;
  private final String method;
  private final Object params;

  public JsonRpcRequest(String method, Object params) {
    this(UUID.randomUUID(), method, params);
  }

  public JsonRpcRequest(UUID id, String method, Object params) {
    this.id = id;
    this.method = method;
    this.params = params;
  }

  public UUID getId() {
    return this.id;
  }

  public String getVersion() {
    return "2.0";
  }

  public String getMethod() {
    return this.method;
  }

  public Object getParams() {
    return this.params;
  }
}
