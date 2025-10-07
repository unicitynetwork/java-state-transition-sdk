package org.unicitylabs.sdk.predicate;

/**
 * Predicate engine structure.
 */
public interface PredicateEngine {

  /**
   * Create predicate from serializable predicate.
   *
   * @param predicate serializable predicate.
   * @return parsed predicate
   */
  Predicate create(SerializablePredicate predicate);
}
