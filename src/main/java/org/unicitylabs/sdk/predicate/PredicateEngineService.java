package org.unicitylabs.sdk.predicate;

import java.util.HashMap;
import org.unicitylabs.sdk.predicate.embedded.EmbeddedPredicateEngine;

/**
 * Predefined predicate engines service to create predicates.
 */
public class PredicateEngineService {

  private static final HashMap<PredicateEngineType, PredicateEngine> ENGINES = new HashMap<>() {
    {
      put(PredicateEngineType.EMBEDDED, new EmbeddedPredicateEngine());
    }
  };

  private PredicateEngineService() {
  }

  /**
   * Create predicate from serializable predicate.
   *
   * @param predicate serializable predicate
   * @return parsed predicate
   */
  public static Predicate createPredicate(SerializablePredicate predicate) {
    PredicateEngine engine = PredicateEngineService.ENGINES.get(predicate.getEngine());
    if (engine == null) {
      throw new IllegalArgumentException(
          "Unsupported predicate engine type: " + predicate.getEngine());
    }

    return engine.create(predicate);
  }


}
