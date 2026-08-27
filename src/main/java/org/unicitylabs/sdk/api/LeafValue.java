package org.unicitylabs.sdk.api;

import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.crypto.hash.DataHasher;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;

/**
 * Sparse Merkle tree leaf value recorded by the Unicity Service for an accepted
 * certification request.
 *
 * <p>The value binds the reference time the request was validated under, not the transaction
 * hash alone. The tree is append-only, so a leaf can be certified afresh against any later
 * root and a later inclusion proof carries a later round's reference time. Binding the
 * reference time into the leaf value fixes the value the transition was validated under, for
 * any proof of that leaf.
 */
public final class LeafValue {

  private LeafValue() {
  }

  /**
   * Calculate the leaf value for a certified request.
   *
   * @param transactionHash transaction hash of the certified request
   * @param referenceTime reference time of the round the request was validated in
   *
   * @return leaf value
   */
  public static DataHash calculate(DataHash transactionHash, long referenceTime) {
    return new DataHasher(HashAlgorithm.SHA256)
            .update(
                    CborSerializer.encodeArray(
                            CborSerializer.encodeByteString(transactionHash.getData()),
                            CborSerializer.encodeUnsignedInteger(referenceTime)
                    )
            )
            .digest();
  }
}
