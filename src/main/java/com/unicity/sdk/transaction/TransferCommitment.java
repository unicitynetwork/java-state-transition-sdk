
package com.unicity.sdk.transaction;

import com.unicity.sdk.address.Address;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.token.Token;
import java.util.Objects;

/**
 * Commitment representing a transfer transaction
 */
public class TransferCommitment extends Commitment<TransferTransactionData> {

  public TransferCommitment(RequestId requestId, TransferTransactionData transactionData,
      Authenticator authenticator) {
    super(requestId, transactionData, authenticator);
  }

  public static TransferCommitment create(
      Token<?> token,
      Address recipient,
      byte[] salt,
      DataHash dataHash,
      byte[] message,
      SigningService signingService
  ) {
    Objects.requireNonNull(token, "Token cannot be null");
    Objects.requireNonNull(recipient, "Recipient address cannot be null");
    Objects.requireNonNull(salt, "Salt cannot be null");
    Objects.requireNonNull(signingService, "SigningService cannot be null");

    TransferTransactionData transactionData = new TransferTransactionData(
        token.getState(), recipient, salt, dataHash, message, token.getNametags());

    DataHash sourceStateHash = transactionData.getSourceState()
        .calculateHash(token.getId(), token.getType());
    DataHash transactionHash = transactionData.calculateHash(token.getId(), token.getType());

    RequestId requestId = RequestId.create(signingService.getPublicKey(), sourceStateHash);
    Authenticator authenticator = Authenticator.create(signingService, transactionHash,
        sourceStateHash);

    return new TransferCommitment(requestId, transactionData, authenticator);
  }

  public Transaction<TransferTransactionData> toTransaction(Token<?> token,
      InclusionProof inclusionProof) {
    if (inclusionProof.verify(this.getRequestId()) != InclusionProofVerificationStatus.OK) {
      throw new RuntimeException("Inclusion proof verification failed.");
    }

    if (inclusionProof.getAuthenticator().isEmpty()) {
      throw new RuntimeException("Authenticator is missing from inclusion proof.");
    }

    if (!this.getTransactionData().calculateHash(token.getId(), token.getType())
        .equals(inclusionProof.getTransactionHash().orElse(null))) {
      throw new RuntimeException("Payload hash mismatch.");
    }

    return new Transaction<>(this.getTransactionData(), inclusionProof);
  }
}
