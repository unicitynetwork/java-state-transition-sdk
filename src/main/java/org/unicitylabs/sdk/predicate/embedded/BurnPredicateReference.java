package org.unicitylabs.sdk.predicate.embedded;

import java.util.Objects;
import org.unicitylabs.sdk.address.DirectAddress;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.PredicateReference;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.token.TokenType;

/**
 * Burn predicate reference.
 */
public class BurnPredicateReference implements PredicateReference {

  private final DataHash hash;

  private BurnPredicateReference(DataHash hash) {
    this.hash = hash;
  }

  /**
   * Get burn predicate reference hash.
   *
   * @return reference hash
   */
  public DataHash getHash() {
    return this.hash;
  }

  /**
   * Create burn predicate reference.
   *
   * @param tokenType  token type
   * @param burnReason burn reason
   * @return predicate reference
   */
  public static BurnPredicateReference create(TokenType tokenType, DataHash burnReason) {
    Objects.requireNonNull(tokenType, "Token type cannot be null");
    Objects.requireNonNull(burnReason, "Burn reason cannot be null");

    return new BurnPredicateReference(
        new DataHasher(HashAlgorithm.SHA256)
            .update(
                CborSerializer.encodeArray(
                    CborSerializer.encodeByteString(EmbeddedPredicateType.BURN.getBytes()),
                    CborSerializer.encodeByteString(tokenType.toCbor()),
                    CborSerializer.encodeByteString(burnReason.getImprint())
                )
            )
            .digest()
    );
  }

  /**
   * Convert predicate reference to address.
   *
   * @return predicate address
   */
  public DirectAddress toAddress() {
    return DirectAddress.create(this.hash);
  }
}
