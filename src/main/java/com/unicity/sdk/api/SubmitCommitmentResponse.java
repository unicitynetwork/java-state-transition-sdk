package com.unicity.sdk.api;

public class SubmitCommitmentResponse {

  private final SubmitCommitmentStatus status;

  public SubmitCommitmentResponse(SubmitCommitmentStatus status) {
    this.status = status;
  }

  public SubmitCommitmentStatus getStatus() {
    return this.status;
  }

  @Override
  public String toString() {
    return String.format("SubmitCommitmentResponse{status=%s}", this.status);
  }
}
