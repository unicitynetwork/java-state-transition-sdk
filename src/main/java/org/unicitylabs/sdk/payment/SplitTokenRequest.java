package org.unicitylabs.sdk.payment;

import org.unicitylabs.sdk.payment.asset.Asset;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.transaction.TokenSalt;
import org.unicitylabs.sdk.transaction.TokenType;

import java.util.Objects;
import java.util.Set;

/**
 * Request to mint one new token as part of a token split.
 */
public class SplitTokenRequest {

  private final Predicate recipient;
  private final TokenType tokenType;
  private final Set<Asset> assets;
  private final TokenSalt salt;

  private SplitTokenRequest(Predicate recipient, TokenType tokenType, Set<Asset> assets, TokenSalt salt) {
    this.recipient = recipient;
    this.tokenType = tokenType;
    this.assets = Set.copyOf(assets);
    this.salt = salt;
  }

  /**
   * Create a split token request.
   *
   * @param recipient predicate that will lock the new token
   * @param assets assets the new token will receive
   * @param tokenType token type for the new token
   * @param salt salt for the new token
   *
   * @return split token request
   */
  public static SplitTokenRequest create(
          Predicate recipient,
          Set<Asset> assets,
          TokenType tokenType,
          TokenSalt salt
  ) {
    Objects.requireNonNull(recipient, "Recipient cannot be null");
    Objects.requireNonNull(assets, "Assets cannot be null");
    Objects.requireNonNull(tokenType, "Token type cannot be null");
    Objects.requireNonNull(salt, "Salt cannot be null");

    return new SplitTokenRequest(recipient, tokenType, assets, salt);
  }

  /**
   * Create a split token request with a random salt.
   *
   * @param recipient predicate that will lock the new token
   * @param assets assets the new token will receive
   * @param tokenType token type for the new token
   *
   * @return split token request
   */
  public static SplitTokenRequest create(Predicate recipient, Set<Asset> assets, TokenType tokenType) {
    return SplitTokenRequest.create(recipient, assets, tokenType, TokenSalt.generate());
  }

  /**
   * Create a split token request with a random token type and salt.
   *
   * @param recipient predicate that will lock the new token
   * @param assets assets the new token will receive
   *
   * @return split token request
   */
  public static SplitTokenRequest create(Predicate recipient, Set<Asset> assets) {
    return SplitTokenRequest.create(recipient, assets, TokenType.generate(), TokenSalt.generate());
  }

  public Predicate getRecipient() {
    return this.recipient;
  }

  public TokenType getTokenType() {
    return this.tokenType;
  }

  public Set<Asset> getAssets() {
    return this.assets;
  }

  public TokenSalt getSalt() {
    return this.salt;
  }
}