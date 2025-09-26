
package org.unicitylabs.sdk.transaction;

import java.util.Objects;
import org.unicitylabs.sdk.api.Authenticator;
import org.unicitylabs.sdk.api.RequestId;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.util.HexConverter;

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
    return SigningService.createFromMaskedSecret(MINTER_SECRET, transactionData.getTokenId().getBytes());
  }
}
