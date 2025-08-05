package com.unicity.sdk.token;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TokenVerificationResult {

  private final boolean isSuccessful;
  private final List<TokenVerificationResult> results;
  private final String message;

  private TokenVerificationResult(boolean isSuccessful, String message,
      List<TokenVerificationResult> results) {
    this.message = message;
    this.results = List.copyOf(results);
    this.isSuccessful = isSuccessful;
  }

  public static TokenVerificationResult success() {
    return new TokenVerificationResult(true, "Verification successful", List.of());
  }

  public static TokenVerificationResult fail(String error) {
    return new TokenVerificationResult(false, error, List.of());
  }

  public static TokenVerificationResult fromChildren(String message,
      List<TokenVerificationResult> children) {
    return new TokenVerificationResult(
        children.stream().allMatch(TokenVerificationResult::isSuccessful), message, children);
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
