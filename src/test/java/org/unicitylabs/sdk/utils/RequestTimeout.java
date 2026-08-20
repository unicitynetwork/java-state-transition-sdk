package org.unicitylabs.sdk.utils;

/**
 * Certification request timeouts for tests.
 */
public final class RequestTimeout {

  private RequestTimeout() {
  }

  /**
   * A timeout an hour ahead of the current wall clock, so no test run can reach it while the
   * request is in flight.
   *
   * @return request timeout in Unix seconds
   */
  public static long requestTimeout() {
    return System.currentTimeMillis() / 1000 + 3600;
  }

  /**
   * A timeout that has already passed, for exercising the expiry path.
   *
   * @return request timeout in Unix seconds
   */
  public static long expiredRequestTimeout() {
    return System.currentTimeMillis() / 1000 - 3600;
  }
}
