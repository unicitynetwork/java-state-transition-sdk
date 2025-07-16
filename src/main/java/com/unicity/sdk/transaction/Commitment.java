
package com.unicity.sdk.transaction;

import com.unicity.sdk.api.Authenticator;
import com.unicity.sdk.api.RequestId;
import com.unicity.sdk.signing.SigningService;

/**
 * Commitment representing a submitted transaction
 *
 * @param <T> the type of transaction data
 */
public class Commitment<T extends TransactionData<?>> {

  private final RequestId requestId;
  private final T transactionData;
  private final Authenticator authenticator;

  public Commitment(RequestId requestId, T transactionData, Authenticator authenticator) {
    this.requestId = requestId;
    this.transactionData = transactionData;
    this.authenticator = authenticator;
  }

  /**
   * Creates a new `Commitment` instance by generating a request ID and authenticator.
   *
   * @param <T>             The type of transaction data.
   * @param transactionData The data associated with the transaction.
   * @param signingService  The service used to sign the transaction.
   * @return Commitment for transaction.
   */
  public static <T extends TransactionData<?>> Commitment<T> create(
      T transactionData,
      SigningService signingService
  ) {
    RequestId requestId = RequestId.create(signingService.getPublicKey(),
        transactionData.getSourceState().getHash());
    Authenticator authenticator = Authenticator.create(
        signingService,
        transactionData.getHash(),
        transactionData.getSourceState().getHash()
    );

    return new Commitment<T>(requestId, transactionData, authenticator);
  }

  public RequestId getRequestId() {
    return requestId;
  }

  public T getTransactionData() {
    return transactionData;
  }

  public Authenticator getAuthenticator() {
    return authenticator;
  }
}
