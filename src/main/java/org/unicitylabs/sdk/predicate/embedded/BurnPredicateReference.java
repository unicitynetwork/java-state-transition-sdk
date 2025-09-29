package org.unicitylabs.sdk.predicate.embedded;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.Objects;
import org.unicitylabs.sdk.address.DirectAddress;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.PredicateReference;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.token.TokenType;

public class BurnPredicateReference implements PredicateReference {

  private final DataHash hash;

  private BurnPredicateReference(DataHash hash) {
    this.hash = hash;
  }

  public DataHash getHash() {
    return this.hash;
  }

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

  public DirectAddress toAddress() {
    return DirectAddress.create(this.hash);
  }
}
