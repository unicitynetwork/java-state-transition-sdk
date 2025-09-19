package org.unicitylabs.sdk.predicate;

public interface SerializablePredicate {
  PredicateEngineType getEngine();

  byte[] encode();

  byte[] encodeParameters();
}
