package com.unicity.sdk.serializer.json.predicate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.predicate.Predicate;
import com.unicity.sdk.predicate.MaskedPredicate;
import com.unicity.sdk.predicate.PredicateType;
import java.io.IOException;

public class IPredicateJson {
  private static final String TYPE_FIELD = "type";

  private IPredicateJson() {}

  public static class Deserializer extends JsonDeserializer<Predicate> {
    @Override
    public Predicate deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      JsonNode node = p.getCodec().readTree(p);
      JsonNode typeNode = node.get(TYPE_FIELD);

      if (typeNode == null) {
        throw MismatchedInputException.from(p, Predicate.class, "Missing predicate 'type' field");
      }

      switch (PredicateType.valueOf(typeNode.asText())) {
        case MASKED:
          JsonParser parser = node.traverse(p.getCodec());
          parser.nextToken();
          return ctx.readValue(parser, MaskedPredicate.class);
        case UNMASKED:
//          return p.getCodec().treeToValue(node, UnmaskedPredicateJson.Deserializer.class);
          p.skipChildren();
      }

      return null;
    }
  }
}