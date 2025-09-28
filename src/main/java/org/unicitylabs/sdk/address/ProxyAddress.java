package org.unicitylabs.sdk.address;

import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.util.HexConverter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Proxy address implementation
 */
public class ProxyAddress implements Address {

  private final TokenId data;
  private final byte[] checksum;

  private ProxyAddress(TokenId data, byte[] checksum) {
    this.data = data;
    this.checksum = Arrays.copyOf(checksum, checksum.length);
  }

  /**
   * Create a proxy address from a nametag string
   * @param name the nametag
   * @return the proxy address
   */
  public static ProxyAddress create(String name) {
    return ProxyAddress.create(TokenId.fromNameTag(name));
  }

  /**
   * Create a proxy address from a token ID
   * @param tokenId the token ID
   * @return the proxy address
   */
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

  public static Address resolve(Address inputAddress, List<Token<?>> nametags) {
    Map<Address, Token<?>> nametagMap = new HashMap<>();
    for (Token<?> token : nametags) {
      if (token == null) {
        throw new IllegalArgumentException("Nametag tokens list cannot contain null elements");
      }

      Address address = ProxyAddress.create(token.getId());
      if (nametagMap.containsKey(address)) {
        throw new IllegalArgumentException(
            "Nametag tokens list contains duplicate addresses: " + address);
      }
      nametagMap.put(address, token);
    }

    Address targetAddress = inputAddress;
    while (targetAddress.getScheme() != AddressScheme.DIRECT) {
      Token<?> nametag = nametagMap.get(targetAddress);
      if (nametag == null || nametag.getData().isEmpty()) {
        return null;
      }

      targetAddress = AddressFactory.createAddress(
          new String(nametag.getData().get(), StandardCharsets.UTF_8));
    }

    return targetAddress;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ProxyAddress)) {
      return false;
    }
    ProxyAddress that = (ProxyAddress) o;
    return Objects.equals(this.data, that.data) && Arrays.equals(this.checksum,
        that.checksum);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.data, Arrays.hashCode(this.checksum));
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