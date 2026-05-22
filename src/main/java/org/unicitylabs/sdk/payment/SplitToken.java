package org.unicitylabs.sdk.payment;

import org.unicitylabs.sdk.api.NetworkId;
import org.unicitylabs.sdk.payment.asset.Asset;
import org.unicitylabs.sdk.predicate.Predicate;
import org.unicitylabs.sdk.transaction.TokenSalt;
import org.unicitylabs.sdk.transaction.TokenType;

import java.util.List;
import java.util.Set;

/**
 * Realized split output: all data needed to mint the new token.
 */
public class SplitToken {

  private final NetworkId networkId;
  private final Predicate recipient;
  private final TokenType tokenType;
  private final TokenSalt salt;
  private final Set<Asset> assets;
  private final List<SplitAssetProof> proofs;

  SplitToken(
          NetworkId networkId,
          Predicate recipient,
          TokenType tokenType,
          TokenSalt salt,
          Set<Asset> assets,
          List<SplitAssetProof> proofs
  ) {
    this.networkId = networkId;
    this.recipient = recipient;
    this.tokenType = tokenType;
    this.salt = salt;
    this.assets = Set.copyOf(assets);
    this.proofs = List.copyOf(proofs);
  }

  public NetworkId getNetworkId() {
    return this.networkId;
  }

  public Predicate getRecipient() {
    return this.recipient;
  }

  public TokenType getTokenType() {
    return this.tokenType;
  }

  public TokenSalt getSalt() {
    return this.salt;
  }

  public Set<Asset> getAssets() {
    return this.assets;
  }

  public List<SplitAssetProof> getProofs() {
    return this.proofs;
  }
}