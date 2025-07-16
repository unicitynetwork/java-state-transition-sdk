package com.unicity.sdk.api;

import com.unicity.sdk.hash.DataHash;

public class SubmitCommitmentRequest {

  private final RequestId requestId;
  private final DataHash transactionHash;
  private final Authenticator authenticator;
  private final Boolean receipt;

  public SubmitCommitmentRequest(RequestId requestId, DataHash transactionHash,
      Authenticator authenticator, Boolean receipt) {
    this.requestId = requestId;
    this.transactionHash = transactionHash;
    this.authenticator = authenticator;
    this.receipt = receipt;
  }

  public RequestId getRequestId() {
    return this.requestId;
  }

  public DataHash getTransactionHash() {
    return this.transactionHash;
  }

  public Authenticator getAuthenticator() {
    return this.authenticator;
  }

  public Boolean getReceipt() {
    return this.receipt;
  }
}
