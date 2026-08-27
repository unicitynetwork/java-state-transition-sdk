package org.unicitylabs.sdk.utils;

/**
 * Certification request deadlines for tests.
 */
public final class ExpiresAt {

  private ExpiresAt() {
  }

  /**
   * A deadline an hour ahead of the current wall clock, so no test run can reach it while the
   * request is in flight.
   *
   * @return request deadline in Unix seconds
   */
  public static Long expiresAt() {
    return System.currentTimeMillis() / 1000 + 3600;
  }

  /**
   * A deadline that has already passed, for exercising the expiry path.
   *
   * @return request deadline in Unix seconds
   */
  public static Long expiredExpiresAt() {
    return System.currentTimeMillis() / 1000 - 3600;
  }
}
