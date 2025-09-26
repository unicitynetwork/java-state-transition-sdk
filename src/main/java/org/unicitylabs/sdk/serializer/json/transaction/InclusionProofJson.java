package org.unicitylabs.sdk.serializer.json.transaction;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.bft.UnicityCertificate;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.transaction.InclusionProof;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class InclusionProofJson {

  private static final String MERKLE_TREE_PATH_FIELD = "merkleTreePath";
  private static final String AUTHENTICATOR_FIELD = "authenticator";
  private static final String TRANSACTION_HASH_FIELD = "transactionHash";
  private static final String UNICITY_CERTIFICATE_FIELD = "unicityCertificate";

  private InclusionProofJson() {
  }

  public static class Serializer extends JsonSerializer<InclusionProof> {

    @Override
    public void serialize(InclusionProof value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException {
      if (value == null) {
        gen.writeNull();
        return;
      }

      gen.writeStartObject();
      gen.writeObjectField(MERKLE_TREE_PATH_FIELD, value.getMerkleTreePath());
      gen.writeObjectField(AUTHENTICATOR_FIELD, value.getAuthenticator());
      gen.writeObjectField(TRANSACTION_HASH_FIELD, value.getTransactionHash());
      gen.writeObjectField(
          UNICITY_CERTIFICATE_FIELD,
          UnicityObjectMapper.CBOR.writeValueAsBytes(value.getUnicityCertificate())
      );
      gen.writeEndObject();
    }
  }

  public static class Deserializer extends JsonDeserializer<InclusionProof> {

    @Override
    public InclusionProof deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
      SparseMerkleTreePath merkleTreePath = null;
      Authenticator authenticator = null;
      DataHash transactionHash = null;
      UnicityCertificate unicityCertificate = null;

      Set<String> fields = new HashSet<>();

      if (!p.isExpectedStartObjectToken()) {
        throw MismatchedInputException.from(p, InclusionProof.class, "Expected object value");
      }

      while (p.nextToken() != JsonToken.END_OBJECT) {
        String fieldName = p.currentName();

        if (!fields.add(fieldName)) {
          throw MismatchedInputException.from(p, InclusionProof.class,
              String.format("Duplicate field: %s", fieldName));
        }

        p.nextToken();
        try {
          switch (fieldName) {
            case MERKLE_TREE_PATH_FIELD:
              merkleTreePath = p.readValueAs(SparseMerkleTreePath.class);
              break;
            case AUTHENTICATOR_FIELD:
              authenticator =
                  p.currentToken() != JsonToken.VALUE_NULL ? p.readValueAs(Authenticator.class)
                      : null;
              break;
            case TRANSACTION_HASH_FIELD:
              transactionHash =
                  p.currentToken() != JsonToken.VALUE_NULL ? p.readValueAs(DataHash.class) : null;
              break;
            case UNICITY_CERTIFICATE_FIELD:
              byte[] bytes = p.readValueAs(byte[].class);
              unicityCertificate = UnicityObjectMapper.CBOR.readValue(
                  bytes,
                  UnicityCertificate.class
              );
              break;
            default:
              p.skipChildren();
          }
        } catch (Exception e) {
          throw MismatchedInputException.wrapWithPath(e, InclusionProof.class, fieldName);
        }
      }

      Set<String> missingFields = new HashSet<>(
          Set.of(MERKLE_TREE_PATH_FIELD, UNICITY_CERTIFICATE_FIELD)
      );
      missingFields.removeAll(fields);
      if (!missingFields.isEmpty()) {
        throw MismatchedInputException.from(p, Token.class,
            String.format("Missing required fields: %s", missingFields));
      }

      return new InclusionProof(merkleTreePath, authenticator, transactionHash, unicityCertificate);
    }
  }
}
