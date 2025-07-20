package com.unicity.sdk.serializer.json.predicate;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.predicate.IPredicate;
import com.unicity.sdk.predicate.MaskedPredicate;
import com.unicity.sdk.predicate.PredicateType;
import com.unicity.sdk.smt.path.MerkleTreePath;
import com.unicity.sdk.smt.path.MerkleTreePathStep;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IPredicateJson {
  private static final String TYPE_FIELD = "type";

  private IPredicateJson() {}

  public static class Deserializer extends JsonDeserializer<IPredicate> {
    @Override
    public IPredicate deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      JsonNode node = p.getCodec().readTree(p);
      JsonNode typeNode = node.get(TYPE_FIELD);

      if (typeNode == null) {
        throw MismatchedInputException.from(p, IPredicate.class, "Missing predicate 'type' field");
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