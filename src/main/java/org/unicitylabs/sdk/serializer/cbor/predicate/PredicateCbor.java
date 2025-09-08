package org.unicitylabs.sdk.serializer.cbor.predicate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.unicitylabs.sdk.predicate.BurnPredicate;
import org.unicitylabs.sdk.predicate.MaskedPredicate;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.predicate.PredicateType;
import org.unicitylabs.sdk.predicate.UnmaskedPredicate;
import java.io.IOException;

public class PredicateCbor {

  private PredicateCbor() {
  }

  public static class Deserializer extends JsonDeserializer<Predicate> {

    @Override
    public Predicate deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      if (!p.isExpectedStartArrayToken()) {
        throw MismatchedInputException.from(p, Predicate.class, "Expected array value");
      }

      JsonNode node = p.getCodec().readTree(p);
      JsonNode typeNode = node.get(0);

      if (typeNode == null) {
        throw MismatchedInputException.from(p, Predicate.class, "Missing predicate 'type' field");
      }

      switch (PredicateType.valueOf(typeNode.asText())) {
        case MASKED:
        {
          JsonParser parser = node.traverse(p.getCodec());
          parser.nextToken();
          return ctx.readValue(parser, MaskedPredicate.class);
        }
        case UNMASKED:
        {
          JsonParser parser = node.traverse(p.getCodec());
          parser.nextToken();
          return ctx.readValue(parser, UnmaskedPredicate.class);
        }
        case BURN: {
          JsonParser parser = node.traverse(p.getCodec());
          parser.nextToken();
          return ctx.readValue(parser, BurnPredicate.class);
        }
        default:
          p.skipChildren();
      }

      return null;
    }
  }
}