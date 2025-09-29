
package org.unicitylabs.sdk.address;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Address interface
 */
@JsonSerialize(using = AddressJson.Serializer.class)
@JsonDeserialize(using = AddressJson.Deserializer.class)
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
