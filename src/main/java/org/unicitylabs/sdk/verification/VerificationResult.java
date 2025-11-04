package org.unicitylabs.sdk.verification;

import java.util.List;

/**
 * Verification result implementation.
 */
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

  /**
   * Return successful verification result.
   *
   * @return verification result
   */
  public static VerificationResult success() {
    return new VerificationResult(VerificationResultCode.OK, "Verification successful", List.of());
  }

  /**
   * Return successful verification result with child results.
   *
   * @param results child results
   * @return verification result
   */
  public static VerificationResult success(List<VerificationResult> results) {
    return new VerificationResult(VerificationResultCode.OK, "Verification successful", results);
  }

  /**
   * Return failed verification result.
   *
   * @param error error message
   * @return verification result
   */
  public static VerificationResult fail(String error) {
    return new VerificationResult(VerificationResultCode.FAIL, error, List.of());
  }

  /**
   * Return failed verification result with child results.
   *
   * @param error   error message
   * @param results child results
   * @return verification result
   */
  public static VerificationResult fail(String error, List<VerificationResult> results) {
    return new VerificationResult(VerificationResultCode.FAIL, error, results);
  }

  /**
   * Create verification result from child results, all has to succeed.
   *
   * @param message  message for the verification result
   * @param children child results
   * @return verification result
   */
  public static VerificationResult fromChildren(
      String message,
      List<VerificationResult> children
  ) {
    return new VerificationResult(
        children.stream().allMatch(VerificationResult::isSuccessful)
            ? VerificationResultCode.OK
            : VerificationResultCode.FAIL,
        message,
        children
    );
  }

  /**
   * Is verification successful.
   *
   * @return success if verification status is ok
   */
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
