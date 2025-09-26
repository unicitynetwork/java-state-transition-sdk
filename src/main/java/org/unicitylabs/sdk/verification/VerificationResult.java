package org.unicitylabs.sdk.verification;

import java.util.List;

public class VerificationResult {

  private final VerificationResultCode status;
  private final List<VerificationResult> results;
  private final String message;

  private VerificationResult(VerificationResultCode status, String message,
      List<VerificationResult> results) {
    this.message = message;
    this.results = List.copyOf(results);
    this.status = status;
  }

  public static VerificationResult success() {
    return new VerificationResult(VerificationResultCode.OK, "Verification successful", List.of());
  }

  public static VerificationResult success(List<VerificationResult> results) {
    return new VerificationResult(VerificationResultCode.OK, "Verification successful", results);
  }

  public static VerificationResult fail(String error) {
    return new VerificationResult(VerificationResultCode.FAIL, error, List.of());
  }

  public static VerificationResult fail(String error, List<VerificationResult> results) {
    return new VerificationResult(VerificationResultCode.FAIL, error, results);
  }

  public static VerificationResult fromChildren(String message,
      List<VerificationResult> children) {
    return new VerificationResult(
        children.stream().allMatch(VerificationResult::isSuccessful)
            ? VerificationResultCode.OK
            : VerificationResultCode.FAIL,
        message,
        children
    );
  }

  public boolean isSuccessful() {
    return this.status == VerificationResultCode.OK;
  }

  @Override
  public String toString() {
    return String.format(
        "TokenVerificationResult{isSuccessful=%s, message='%s', results=%s}",
        this.status, this.message, this.results
    );
  }
}
