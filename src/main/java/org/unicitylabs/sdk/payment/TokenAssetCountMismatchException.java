package org.unicitylabs.sdk.payment;

/**
 * Thrown when the split requests do not cover exactly the source token's assets.
 */
public class TokenAssetCountMismatchException extends IllegalArgumentException {

  /**
   * Create the exception.
   */
  public TokenAssetCountMismatchException() {
    super("Token and split tokens asset counts differ.");
  }
}
