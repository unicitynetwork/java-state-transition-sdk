package org.unicitylabs.sdk.predicate.embedded;

import java.io.IOException;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.predicate.PredicateEngine;
import org.unicitylabs.sdk.predicate.SerializablePredicate;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;

public class EmbeddedPredicateEngine implements PredicateEngine {

  public Predicate create(SerializablePredicate predicate) {
    try {
      EmbeddedPredicateType type = EmbeddedPredicateType.fromBytes(predicate.encode());
      switch (type) {
        case MASKED:
          if (predicate instanceof MaskedPredicate) {
            return (MaskedPredicate) predicate;
          }

          return UnicityObjectMapper.CBOR.readValue(
              predicate.encodeParameters(),
              MaskedPredicate.class
          );
        case UNMASKED:
          if (predicate instanceof UnmaskedPredicate) {
            return (UnmaskedPredicate) predicate;
          }

          return UnicityObjectMapper.CBOR.readValue(
              predicate.encodeParameters(),
              UnmaskedPredicate.class
          );
        case BURN:
          if (predicate instanceof BurnPredicate) {
            return (BurnPredicate) predicate;
          }

          return UnicityObjectMapper.CBOR.readValue(
              predicate.encodeParameters(),
              BurnPredicate.class
          );
        default:
          throw new IllegalArgumentException("Unknown predicate type: " + type);
      }
    } catch (IOException e) {
      throw new RuntimeException("Failed to create predicate", e);
    }
  }
}
