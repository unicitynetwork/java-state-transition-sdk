package org.unicitylabs.sdk.address;

import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.util.HexConverter;

import java.util.Arrays;
import java.util.Objects;

/**
 * Direct address implementation
 */
public class DirectAddress implements Address {

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
  public AddressScheme getScheme() {
    return AddressScheme.DIRECT;
  }

  @Override
  public String getAddress() {
    return this.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DirectAddress)) {
      return false;
    }
    DirectAddress that = (DirectAddress) o;
    return Objects.equals(this.data, that.data) && Arrays.equals(this.checksum,
        that.checksum);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.data, Arrays.hashCode(checksum));
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