package org.unicitylabs.sdk.predicate;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Serializable predicate structure.
 */
@JsonSerialize(using = SerializablePredicateJson.Serializer.class)
@JsonDeserialize(using = SerializablePredicateJson.Deserializer.class)
public interface SerializablePredicate {

  /**
   * Get predicate engine.
   *
   * @return predicate engine
   */
  PredicateEngineType getEngine();

  /**
   * Get predicate code as bytes.
   *
   * @return predicate code bytes
   */
  byte[] encode();

  /**
   * Get predicate parameters as bytes.
   *
   * @return parameters bytes
   */
  byte[] encodeParameters();
}
