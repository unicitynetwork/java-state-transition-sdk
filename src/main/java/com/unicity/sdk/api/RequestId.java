package com.unicity.sdk.api;

import com.unicity.sdk.Hashable;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.util.BitString;

/**
 * Represents a unique request identifier derived from a public key and state hash.
 */
public class RequestId implements Hashable {

  private final DataHash hash;

  /**
   * Constructs a RequestId instance.
   *
   * @param hash The DataHash representing the request ID.
   */
  public RequestId(DataHash hash) {
    this.hash = hash;
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
    return BitString.fromDataHash(this.hash);
  }

  /**
   * Gets the underlying DataHash.
   *
   * @return The DataHash.
   */
  public DataHash getHash() {
    return this.hash;
  }

  /**
   * Checks if this RequestId is equal to another.
   *
   * @param obj The object to compare.
   * @return True if equal, false otherwise.
   */
  @Override
  public boolean equals(Object obj) {
      if (this == obj) {
          return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
          return false;
      }
    RequestId requestId = (RequestId) obj;
    return this.hash.equals(requestId.hash);
  }

  /**
   * Returns a string representation of the RequestId.
   *
   * @return The string representation.
   */
  @Override
  public String toString() {
    return String.format("RequestId[%s]", this.hash.toString());
  }
}