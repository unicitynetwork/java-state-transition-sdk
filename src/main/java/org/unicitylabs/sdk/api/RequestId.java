package org.unicitylabs.sdk.api;

import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.util.BitString;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Represents a unique request identifier derived from a public key and state hash.
 */
public class RequestId extends DataHash {

  /**
   * Constructs a RequestId instance.
   *
   * @param hash The DataHash representing the request ID.
   */
  public RequestId(DataHash hash) {
    super(hash.getAlgorithm(), hash.getData());
  }

  /**
   * Creates a RequestId from a public key and state hash.
   *
   * @param id        The public key as a byte array.
   * @param stateHash The state hash.
   * @return A CompletableFuture resolving to a RequestId instance.
   */
  public static RequestId create(byte[] id, DataHash stateHash) {
    return createFromImprint(id, stateHash.getImprint());
  }

  /**
   * Creates a RequestId from a public key and hash imprint.
   *
   * @param id          The public key as a byte array.
   * @param hashImprint The hash imprint as a byte array.
   * @return A CompletableFuture resolving to a RequestId instance.
   */
  public static RequestId createFromImprint(byte[] id, byte[] hashImprint) {
    DataHasher hasher = new DataHasher(HashAlgorithm.SHA256);
    hasher.update(id);
    hasher.update(hashImprint);

    return new RequestId(hasher.digest());
  }

  /**
   * Converts the RequestId to a BitString.
   *
   * @return The BitString representation of the RequestId.
   */
  public BitString toBitString() {
    return BitString.fromDataHash(this);
  }

  /**
   * Returns a string representation of the RequestId.
   *
   * @return The string representation.
   */
  @Override
  public String toString() {
    return String.format("RequestId[%s]", HexConverter.encode(this.getImprint()));
  }
}