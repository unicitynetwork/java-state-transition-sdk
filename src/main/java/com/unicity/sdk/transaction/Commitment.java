
package com.unicity.sdk.transaction;

import com.unicity.sdk.address.Address;
import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.hash.DataHash;
import com.unicity.sdk.signing.SigningService;
import com.unicity.sdk.token.Token;
import com.unicity.sdk.util.HexConverter;
import java.io.IOException;
import java.util.Objects;

/**
 * Commitment representing a submitted transaction
 *
 * @param <T> the type of transaction data
 */
public class Commitment<T extends TransactionData<?>> {
  public static final byte[] MINTER_SECRET = HexConverter.decode(
      "495f414d5f554e4956455253414c5f4d494e5445525f464f525f");

  private final RequestId requestId;
  private final T transactionData;
  private final Authenticator authenticator;

  public Commitment(RequestId requestId, T transactionData, Authenticator authenticator) {
    this.requestId = requestId;
    this.transactionData = transactionData;
    this.authenticator = authenticator;
  }

  public static <T extends MintTransactionData<?>> Commitment<T> create(
      T transactionData
  ) throws IOException {
    SigningService signingService = SigningService.createFromSecret(MINTER_SECRET,
        transactionData.getTokenId().getBytes());

    return Commitment.create(signingService, transactionData, transactionData.calculateHash(),
        transactionData.getSourceState().getHash());
  }

  public static Commitment<TransferTransactionData> create(
      Token<?> token,
      Address recipient,
      byte[] salt,
      DataHash dataHash,
      byte[] message,
      SigningService signingService
  ) throws IOException {
    TransferTransactionData transactionData = new TransferTransactionData(
        token.getState(), recipient, salt, dataHash, message, token.getNametags());
    return Commitment.create(signingService, transactionData,
        transactionData.calculateHash(token.getId(), token.getType()),
        transactionData.getSourceState().calculateHash(token.getId(), token.getType()));
  }

  private static <T extends TransactionData<?>> Commitment<T> create(
      SigningService signingService,
      T transactionData,
      DataHash transactionHash,
      DataHash sourceStateHash) {
    RequestId requestId = RequestId.create(signingService.getPublicKey(), sourceStateHash);
    Authenticator authenticator = Authenticator.create(signingService, transactionHash,
        sourceStateHash);
    return new Commitment<>(requestId, transactionData, authenticator);
  }

  /**
   * Returns the request ID associated with this commitment.
   *
   * @return request ID
   */
  public RequestId getRequestId() {
    return requestId;
  }

  /**
   * Returns the transaction data associated with this commitment.
   *
   * @return transaction data
   */
  public T getTransactionData() {
    return transactionData;
  }

  /**
   * Returns the authenticator associated with this commitment.
   *
   * @return authenticator
   */
  public Authenticator getAuthenticator() {
    return authenticator;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Commitment)) {
      return false;
    }
    Commitment<?> that = (Commitment<?>) o;
    return Objects.equals(this.requestId, that.requestId) && Objects.equals(
        this.transactionData, that.transactionData) && Objects.equals(this.authenticator,
        that.authenticator);
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.requestId, this.transactionData, authenticator);
  }

  @Override
  public String toString() {
    return String.format("Commitment{requestId=%s, transactionData=%s, authenticator=%s}",
        this.requestId, this.transactionData, this.authenticator);
  }
}
