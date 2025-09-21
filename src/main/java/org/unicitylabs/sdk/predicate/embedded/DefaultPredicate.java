package org.unicitylabs.sdk.predicate.embedded;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.predicate.EncodedPredicate;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.predicate.PredicateEngineType;
import org.unicitylabs.sdk.predicate.PredicateReference;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.token.Token;
import org.unicitylabs.sdk.token.TokenId;
import org.unicitylabs.sdk.token.TokenType;
import org.unicitylabs.sdk.transaction.InclusionProof;
import org.unicitylabs.sdk.transaction.InclusionProofVerificationStatus;
import org.unicitylabs.sdk.transaction.Transaction;
import org.unicitylabs.sdk.transaction.TransferTransactionData;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Base class for unmasked and masked predicates
 */
public abstract class DefaultPredicate implements Predicate {

  private final EmbeddedPredicateType type;
  private final TokenId tokenId;
  private final TokenType tokenType;
  private final byte[] publicKey;
  private final String signingAlgorithm;
  private final HashAlgorithm hashAlgorithm;
  private final byte[] nonce;

  protected DefaultPredicate(
      EmbeddedPredicateType type,
      TokenId tokenId,
      TokenType tokenType,
      byte[] publicKey,
      String signingAlgorithm,
      HashAlgorithm hashAlgorithm,
      byte[] nonce) {
    Objects.requireNonNull(type, "Predicate type cannot be null");
    Objects.requireNonNull(tokenId, "TokenId cannot be null");
    Objects.requireNonNull(tokenType, "TokenType cannot be null");
    Objects.requireNonNull(publicKey, "Public key cannot be null");
    Objects.requireNonNull(signingAlgorithm, "Signing algorithm cannot be null");
    Objects.requireNonNull(hashAlgorithm, "Hash algorithm cannot be null");
    Objects.requireNonNull(nonce, "Nonce cannot be null");

    this.type = type;
    this.tokenId = tokenId;
    this.tokenType = tokenType;
    this.publicKey = Arrays.copyOf(publicKey, publicKey.length);
    this.signingAlgorithm = signingAlgorithm;
    this.hashAlgorithm = hashAlgorithm;
    this.nonce = Arrays.copyOf(nonce, nonce.length);
  }

  public EmbeddedPredicateType getType() {
    return this.type;
  }

  public TokenId getTokenId() {
    return this.tokenId;
  }

  public TokenType getTokenType() {
    return this.tokenType;
  }

  public byte[] getPublicKey() {
    return Arrays.copyOf(this.publicKey, this.publicKey.length);
  }

  public String getSigningAlgorithm() {
    return this.signingAlgorithm;
  }

  public HashAlgorithm getHashAlgorithm() {
    return this.hashAlgorithm;
  }

  public byte[] getNonce() {
    return Arrays.copyOf(this.nonce, this.nonce.length);
  }

  @Override
  public DataHash calculateHash() {
    ArrayNode node = UnicityObjectMapper.CBOR.createArrayNode();
    node.addPOJO(this.getReference().getHash());
    node.addPOJO(this.tokenId);
    node.add(this.getNonce());

    try {
      return new DataHasher(HashAlgorithm.SHA256)
          .update(UnicityObjectMapper.CBOR.writeValueAsBytes(node))
          .digest();
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public abstract PredicateReference getReference();

  @Override
  public boolean isOwner(byte[] publicKey) {
    return Arrays.equals(this.publicKey, publicKey);
  }

  @Override
  public boolean verify(Token<?> token, Transaction<TransferTransactionData> transaction) {
    if (!this.tokenId.equals(token.getId()) || !this.tokenType.equals(token.getType())) {
      return false;
    }

    Authenticator authenticator = transaction.getInclusionProof().getAuthenticator().orElse(null);

    if (authenticator == null) {
      return false;
    }

    if (!Arrays.equals(authenticator.getPublicKey(), this.publicKey)) {
      return false;
    }

    DataHash transactionHash = transaction.getData().calculateHash();
    if (!authenticator.verify(transactionHash)) {
      return false;
    }

    RequestId requestId = RequestId.create(
        this.publicKey,
        transaction.getData().getSourceState().calculateHash()
    );
    return transaction.getInclusionProof().verify(requestId) == InclusionProofVerificationStatus.OK;
  }

  @Override
  public PredicateEngineType getEngine() {
    return PredicateEngineType.EMBEDDED;
  }

  @Override
  public byte[] encode() {
    return this.type.getBytes();
  }

  @Override
  public byte[] encodeParameters() {
    try {
      return UnicityObjectMapper.CBOR.writeValueAsBytes(this);
    } catch (JsonProcessingException e) {
      throw new CborSerializationException(e);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DefaultPredicate)) {
      return false;
    }
    DefaultPredicate that = (DefaultPredicate) o;
    return this.type == that.type && Objects.equals(this.tokenId, that.tokenId)
        && Objects.equals(this.tokenType, that.tokenType)
        && Objects.deepEquals(this.publicKey, that.publicKey)
        && Objects.equals(this.signingAlgorithm, that.signingAlgorithm)
        && this.hashAlgorithm == that.hashAlgorithm
        && Arrays.equals(this.nonce, that.nonce);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.type, this.tokenId, this.tokenType, Arrays.hashCode(this.publicKey),
        this.signingAlgorithm, this.hashAlgorithm, Arrays.hashCode(nonce));
  }

  @Override
  public String toString() {
    return String.format(
        "DefaultPredicate{type=%s, tokenId=%s, tokenType=%s, publicKey=%s, algorithm=%s, hashAlgorithm=%s, nonce=%s}",
        this.type,
        this.tokenId,
        this.tokenType,
        HexConverter.encode(this.publicKey),
        this.signingAlgorithm,
        this.hashAlgorithm,
        HexConverter.encode(this.nonce));
  }
}