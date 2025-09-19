package org.unicitylabs.sdk.predicate;

import java.util.HashMap;
import org.unicitylabs.sdk.predicate.embedded.EmbeddedPredicateEngine;

public class PredicateEngineService {

  private static final HashMap<PredicateEngineType, PredicateEngine> ENGINES = new HashMap<>() {
    {
      put(PredicateEngineType.EMBEDDED, new EmbeddedPredicateEngine());
    }
  };

  public static Predicate createPredicate(SerializablePredicate predicate) {
    PredicateEngine engine = PredicateEngineService.ENGINES.get(predicate.getEngine());
    if (engine == null) {
      throw new IllegalArgumentException(
          "Unsupported predicate engine type: " + predicate.getEngine());
    }

    return engine.create(predicate);
  }


}
