package com.unicity.sdk.predicate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.hash.DataHasher;
import com.unicity.sdk.hash.HashAlgorithm;
import com.unicity.sdk.serializer.UnicityObjectMapper;
import com.unicity.sdk.token.TokenId;
import com.unicity.sdk.token.TokenType;
import com.unicity.sdk.transaction.InclusionProofVerificationStatus;
import com.unicity.sdk.transaction.Transaction;
import com.unicity.sdk.transaction.TransferTransactionData;
import com.unicity.sdk.util.HexConverter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Base class for unmasked and masked predicates
 */
public abstract class DefaultPredicate implements Predicate {

  private final PredicateType type;
  private final byte[] publicKey;
  private final String algorithm;
  private final HashAlgorithm hashAlgorithm;
  private final byte[] nonce;

  protected DefaultPredicate(
      PredicateType type,
      byte[] publicKey,
      String algorithm,
      HashAlgorithm hashAlgorithm,
      byte[] nonce) {
    Objects.requireNonNull(type, "Predicate type cannot be null");
    Objects.requireNonNull(publicKey, "Public key cannot be null");
    Objects.requireNonNull(algorithm, "Algorithm cannot be null");
    Objects.requireNonNull(hashAlgorithm, "Hash algorithm cannot be null");
    Objects.requireNonNull(nonce, "Nonce cannot be null");

    this.type = type;
    this.publicKey = Arrays.copyOf(publicKey, publicKey.length);
    this.algorithm = algorithm;
    this.hashAlgorithm = hashAlgorithm;
    this.nonce = Arrays.copyOf(nonce, nonce.length);
  }

  public String getType() {
    return this.type.name();
  }

  public byte[] getPublicKey() {
    return Arrays.copyOf(this.publicKey, this.publicKey.length);
  }

  public String getAlgorithm() {
    return this.algorithm;
  }

  public HashAlgorithm getHashAlgorithm() {
    return this.hashAlgorithm;
  }

  public byte[] getNonce() {
    return Arrays.copyOf(this.nonce, this.nonce.length);
  }

  @Override
  public DataHash calculateHash(TokenId tokenId, TokenType tokenType) {
    ArrayNode node = UnicityObjectMapper.CBOR.createArrayNode();
    node.addPOJO(this.getReference(tokenType).getHash());
    node.addPOJO(tokenId);
    node.add(this.getNonce());

    try {
      return new DataHasher(HashAlgorithm.SHA256)
          .update(UnicityObjectMapper.CBOR.writeValueAsBytes(node))
          .digest();
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  public abstract IPredicateReference getReference(TokenType tokenType);

  @Override
  public boolean isOwner(byte[] publicKey) {
    return Arrays.equals(this.publicKey, publicKey);
  }

  @Override
  public boolean verify(Transaction<TransferTransactionData> transaction, TokenId tokenId,
      TokenType tokenType) {
    Authenticator authenticator = transaction.getInclusionProof().getAuthenticator().orElse(null);
    DataHash transactionHash = transaction.getInclusionProof().getTransactionHash().orElse(null);

    if (authenticator == null || transactionHash == null) {
      return false;
    }

    if (!Arrays.equals(authenticator.getPublicKey(), this.publicKey)) {
      return false;
    }

    if (!authenticator.verify(transaction.getData().calculateHash(tokenId, tokenType))) {
      return false;
    }

    RequestId requestId = RequestId.create(this.publicKey,
        transaction.getData().getSourceState().calculateHash(tokenId, tokenType));
    return transaction.getInclusionProof().verify(requestId) == InclusionProofVerificationStatus.OK;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DefaultPredicate)) {
      return false;
    }
    DefaultPredicate that = (DefaultPredicate) o;
    return type == that.type && Objects.deepEquals(publicKey, that.publicKey)
        && Objects.equals(algorithm, that.algorithm) && hashAlgorithm == that.hashAlgorithm
        && Objects.deepEquals(nonce, that.nonce);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, Arrays.hashCode(publicKey), algorithm, hashAlgorithm,
        Arrays.hashCode(nonce));
  }

  @Override
  public String toString() {
    return String.format(
        "DefaultPredicate{type=%s, publicKey=%s, algorithm=%s, hashAlgorithm=%s, nonce=%s}",
        this.type,
        HexConverter.encode(this.publicKey), this.algorithm, this.hashAlgorithm,
        HexConverter.encode(this.nonce));
  }
}