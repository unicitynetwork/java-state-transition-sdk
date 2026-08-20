package org.unicitylabs.sdk.functional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.TestAggregatorClient;
import org.unicitylabs.sdk.api.CertificationData;
import org.unicitylabs.sdk.api.CertificationStatus;
import org.unicitylabs.sdk.api.NetworkId;
import org.unicitylabs.sdk.crypto.secp256k1.SigningService;
import org.unicitylabs.sdk.predicate.builtin.SignaturePredicate;
import org.unicitylabs.sdk.transaction.MintTransaction;
import org.unicitylabs.sdk.transaction.TokenSalt;
import org.unicitylabs.sdk.transaction.TokenType;
import org.unicitylabs.sdk.utils.RequestTimeout;

/**
 * The Unicity Service admits a request only in a round whose reference time is strictly below
 * the request's timeout.
 */
public class RequestTimeoutTest {

  private final TestAggregatorClient aggregatorClient = TestAggregatorClient.create();
  private final StateTransitionClient client = new StateTransitionClient(this.aggregatorClient);
  private final SignaturePredicate recipient =
          SignaturePredicate.fromSigningService(SigningService.generate());

  private CertificationStatus submit(long timeout) throws Exception {
    MintTransaction transaction = MintTransaction.create(NetworkId.LOCAL, this.recipient, timeout);

    return this.client.submitCertificationRequest(
            CertificationData.fromMintTransaction(transaction)).get().getStatus();
  }

  @Test
  public void acceptsARequestWhoseTimeoutIsAheadOfTheRoundReferenceTime() throws Exception {
    Assertions.assertEquals(CertificationStatus.SUCCESS,
            this.submit(RequestTimeout.requestTimeout()));
  }

  @Test
  public void rejectsARequestWhoseTimeoutTheRoundReferenceTimeHasReached() throws Exception {
    Assertions.assertEquals(CertificationStatus.REQUEST_EXPIRED,
            this.submit(RequestTimeout.expiredRequestTimeout()));
  }

  @Test
  public void bindsTheTimeoutIntoTheTransactionHash() {
    TokenType tokenType = TokenType.generate();
    TokenSalt salt = TokenSalt.generate();

    MintTransaction first = MintTransaction.create(NetworkId.LOCAL, this.recipient, 1755000000L,
            null, tokenType, salt);
    MintTransaction second = MintTransaction.create(NetworkId.LOCAL, this.recipient, 1755000001L,
            null, tokenType, salt);

    Assertions.assertNotEquals(first.calculateTransactionHash(),
            second.calculateTransactionHash());
  }
}
