package com.unicity.sdk.address;

import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenVerificationResult;
import com.unicity.sdk.util.HexConverter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Direct address implementation
 */
public class ProxyAddress implements Address {

  private final TokenId data;
  private final byte[] checksum;

  private ProxyAddress(TokenId data, byte[] checksum) {
    this.data = data;
    this.checksum = Arrays.copyOf(checksum, checksum.length);
  }

  public static ProxyAddress create(TokenId tokenId) {
    DataHash checksum = new DataHasher(HashAlgorithm.SHA256).update(tokenId.getBytes())
        .digest();
    return new ProxyAddress(tokenId, Arrays.copyOf(checksum.getData(), 4));
  }

  @Override
  public AddressScheme getScheme() {
    return AddressScheme.PROXY;
  }

  @Override
  public String getAddress() {
    return this.toString();
  }

  public static Address resolve(Address address, Map<Address, Token<?>> nametags) {
    Address targetAddress = address;
    while (targetAddress.getScheme() != AddressScheme.DIRECT) {
      Token<?> nametag = nametags.get(targetAddress);
      if (nametag == null) {
        return null;
      }

      targetAddress = AddressFactory.createAddress(
          new String(nametag.getState().getData(), StandardCharsets.UTF_8));
    }

    return targetAddress;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ProxyAddress)) {
      return false;
    }
    ProxyAddress that = (ProxyAddress) o;
    return Objects.equals(data, that.data) && Objects.deepEquals(checksum,
        that.checksum);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data, Arrays.hashCode(checksum));
  }

  @Override
  public String toString() {
    return String.format(
        "%s://%s%s",
        AddressScheme.PROXY,
        HexConverter.encode(this.data.getBytes()),
        HexConverter.encode(this.checksum));
  }
}