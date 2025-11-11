package org.unicitylabs.sdk.predicate.embedded;

import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.predicate.PredicateEngine;
import org.unicitylabs.sdk.predicate.SerializablePredicate;

/**
 * Embedded predicate engine implementation.
 */
public class EmbeddedPredicateEngine implements PredicateEngine {

  /**
   * Create embedded predicate engine.
   */
  public EmbeddedPredicateEngine() {}

  /**
   * Create predicate from embedded predicate engine.
   *
   * @param predicate serializable predicate.
   * @return predicate
   */
  public Predicate create(SerializablePredicate predicate) {
    EmbeddedPredicateType type = EmbeddedPredicateType.fromBytes(predicate.encode());
    switch (type) {
      case MASKED:
        if (predicate instanceof MaskedPredicate) {
          return (MaskedPredicate) predicate;
        }

        return MaskedPredicate.fromCbor(predicate.encodeParameters());
      case UNMASKED:
        if (predicate instanceof UnmaskedPredicate) {
          return (UnmaskedPredicate) predicate;
        }

        return UnmaskedPredicate.fromCbor(predicate.encodeParameters());
      case BURN:
        if (predicate instanceof BurnPredicate) {
          return (BurnPredicate) predicate;
        }

        return BurnPredicate.fromCbor(predicate.encodeParameters());
      default:
        throw new IllegalArgumentException("Unknown predicate type: " + type);
    }
  }
}
