package org.unicitylabs.sdk.util;

import java.util.List;

public class VerificationResult {

  private final boolean isSuccessful;
  private final List<VerificationResult> results;
  private final String message;

  private VerificationResult(boolean isSuccessful, String message,
      List<VerificationResult> results) {
    this.message = message;
    this.results = List.copyOf(results);
    this.isSuccessful = isSuccessful;
  }

  public static VerificationResult success() {
    return new VerificationResult(true, "Verification successful", List.of());
  }

  public static VerificationResult fail(String error) {
    return new VerificationResult(false, error, List.of());
  }

  public static VerificationResult fromChildren(String message,
      List<VerificationResult> children) {
    return new VerificationResult(
        children.stream().allMatch(VerificationResult::isSuccessful), message, children);
  }

  public boolean isSuccessful() {
    return this.isSuccessful;
  }

  @Override
  public String toString() {
    return String.format(
        "TokenVerificationResult{isSuccessful=%s, message='%s', results=%s}",
        this.isSuccessful, this.message, this.results
    );
  }
}
