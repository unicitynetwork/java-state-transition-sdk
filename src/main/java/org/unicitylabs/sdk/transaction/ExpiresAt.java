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
   * <p>Without this the range errors surface much later and far from the mistake: a negative value
   * encodes nowhere and fails inside the CBOR serializer while the transaction hash is being
   * computed, and zero encodes fine but produces a request that is expired by construction, since
   * every reference time is at or past it.
   *
   * <p>The accepted range is 1 to {@link Long#MAX_VALUE}. The wire format is a CBOR unsigned
   * integer and so admits values up to 2^64-1, but one at or above 2^63 arrives as a negative long
   * and is rejected here rather than silently reinterpreted. No real deadline comes near that:
   * 2^63 Unix seconds is roughly 292 billion years away.
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
