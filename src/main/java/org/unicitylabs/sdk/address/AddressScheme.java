
package org.unicitylabs.sdk.address;

/**
 * Address scheme.
 */
public enum AddressScheme {
  /**
   * Direct address scheme.
   */
  DIRECT,
  /**
   * Nametag address scheme which redirects to DIRECT scheme eventually.
   */
  PROXY
}
