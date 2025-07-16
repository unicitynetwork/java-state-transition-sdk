package com.unicity.sdk.address;

import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.util.HexConverter;

import java.util.Arrays;

/**
 * Direct address implementation
 */
public class DirectAddress implements IAddress {

  private final DataHash data;
  private final byte[] checksum;

  private DirectAddress(DataHash data, byte[] checksum) {
    this.data = data;
    this.checksum = Arrays.copyOf(checksum, checksum.length);
  }

  public static DirectAddress create(DataHash reference) {
    DataHash checksum = new DataHasher(HashAlgorithm.SHA256).update(reference.getImprint())
        .digest();
    return new DirectAddress(reference, Arrays.copyOf(checksum.getData(), 4));
  }

  @Override
  public String getAddress() {
    return this.toString();
  }

  @Override
  public String toString() {
    return String.format(
        "%s://%s%s",
        AddressScheme.DIRECT,
        HexConverter.encode(this.data.getImprint()),
        HexConverter.encode(this.checksum));
  }
}