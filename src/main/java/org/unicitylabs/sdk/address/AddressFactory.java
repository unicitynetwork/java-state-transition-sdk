package org.unicitylabs.sdk.address;

import java.util.Arrays;
import java.util.Objects;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Factory for creating Address instances from string representations.
 */
public class AddressFactory {

  private AddressFactory() {}

  /**
   * Create an Address from its string representation.
   *
   * @param address The address string.
   * @return The corresponding Address instance.
   * @throws IllegalArgumentException if the address format is invalid or does not match the
   *                                  expected format.
   * @throws NullPointerException     if the address is null.
   */
  public static Address createAddress(String address) {
    Objects.requireNonNull(address, "Address cannot be null");

    String[] result = address.split("://", 2);
    if (result.length != 2) {
      throw new IllegalArgumentException("Invalid address format");
    }

    Address expectedAddress;
    byte[] bytes = HexConverter.decode(result[1]);

    switch (AddressScheme.valueOf(result[0])) {
      case DIRECT:
        expectedAddress = DirectAddress.create(
            DataHash.fromImprint(Arrays.copyOf(bytes, bytes.length - 4)));
        break;
      case PROXY:
        expectedAddress = ProxyAddress.create(new TokenId(Arrays.copyOf(bytes, bytes.length - 4)));
        break;
      default:
        throw new IllegalArgumentException("Invalid address scheme: " + result[0]);
    }

    if (!expectedAddress.getAddress().equals(address)) {
      throw new IllegalArgumentException("Address mismatch");
    }

    return expectedAddress;
  }
}
