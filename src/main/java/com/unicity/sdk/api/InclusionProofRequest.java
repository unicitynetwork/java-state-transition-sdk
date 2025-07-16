package com.unicity.sdk.api;

class InclusionProofRequest {

  private final RequestId requestId;

  public InclusionProofRequest(RequestId requestId) {
    this.requestId = requestId;
  }

  public RequestId getRequestId() {
    return this.requestId;
  }
}
