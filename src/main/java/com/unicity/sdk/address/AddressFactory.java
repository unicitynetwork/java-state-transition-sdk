package com.unicity.sdk.address;

import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.util.HexConverter;
import java.util.Arrays;
import java.util.Objects;

public class AddressFactory {

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
