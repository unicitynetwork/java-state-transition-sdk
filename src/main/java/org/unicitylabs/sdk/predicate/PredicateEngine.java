package org.unicitylabs.sdk.predicate;

public interface PredicateEngine {
  Predicate create(SerializablePredicate predicate);
}
