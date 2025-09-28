
package org.unicitylabs.sdk.address;

/**
 * Address interface
 */
public interface Address {

  /**
   * Get the address scheme
   *
   * @return the address scheme
   */
  AddressScheme getScheme();

  /**
   * Get the address as a string
   *
   * @return the address string
   */
  String getAddress();
}
