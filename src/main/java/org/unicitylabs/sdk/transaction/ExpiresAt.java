package org.unicitylabs.sdk.transaction;

/**
 * Validation for the exclusive request deadline carried by a certification request.
 */
public final class ExpiresAt {

  private ExpiresAt() {
  }

  /**
   * Validate an exclusive request deadline at the boundary that accepts it.
   *
   * <p>Zero encodes fine but is expired by construction, since every reference time is at or past
   * it. The accepted range is 1 to {@link Long#MAX_VALUE}; the wire admits up to 2^64-1, but a
   * value at or above 2^63 arrives as a negative long and is rejected rather than reinterpreted.
   *
   * @param expiresAt deadline in Unix seconds, or null to let the service assign one
   * @return the validated deadline, unchanged
   * @throws IllegalArgumentException if the deadline is not a positive number of Unix seconds
   */
  public static Long validate(Long expiresAt) {
    if (expiresAt == null) {
      return null;
    }

    if (expiresAt <= 0L) {
      throw new IllegalArgumentException(
              String.format("Request deadline must be a positive number of Unix seconds, got %s.",
                      expiresAt));
    }

    return expiresAt;
  }
}
