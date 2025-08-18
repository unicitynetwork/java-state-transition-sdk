
package com.unicity.sdk.transaction;

import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.util.HexConverter;
import java.util.Objects;

/**
 * Commitment representing a submitted transaction
 *
 * @param <T> the type of transaction data
 */
public class MintCommitment<T extends MintTransactionData<?>> extends Commitment<T> {

  public static final byte[] MINTER_SECRET = HexConverter.decode(
      "495f414d5f554e4956455253414c5f4d494e5445525f464f525f");

  public MintCommitment(RequestId requestId, T transactionData, Authenticator authenticator) {
    super(requestId, transactionData, authenticator);
  }

  public static <T extends MintTransactionData<?>> MintCommitment<T> create(
      T transactionData
  ) {
    Objects.requireNonNull(transactionData, "Transaction data cannot be null");

    SigningService signingService = MintCommitment.createSigningService(transactionData);

    DataHash sourceStateHash = transactionData.getSourceState().getHash();
    DataHash transactionHash = transactionData.calculateHash();

    RequestId requestId = RequestId.create(signingService.getPublicKey(), sourceStateHash);
    Authenticator authenticator = Authenticator.create(signingService, transactionHash,
        sourceStateHash);

    return new MintCommitment<>(requestId, transactionData, authenticator);
  }

  public static SigningService createSigningService(MintTransactionData<?> transactionData) {
    return SigningService.createFromSecret(MINTER_SECRET, transactionData.getTokenId().getBytes());
  }

  public Transaction<T> toTransaction(InclusionProof inclusionProof) {
    if (inclusionProof.verify(this.getRequestId()) != InclusionProofVerificationStatus.OK) {
      throw new RuntimeException("Inclusion proof verification failed.");
    }

    if (inclusionProof.getAuthenticator().isEmpty()) {
      throw new RuntimeException("Authenticator is missing from inclusion proof.");
    }

    if (!this.getTransactionData().calculateHash().equals(inclusionProof.getTransactionHash().orElse(null))) {
      throw new RuntimeException("Payload hash mismatch.");
    }

    return new Transaction<>(this.getTransactionData(), inclusionProof);
  }
}
