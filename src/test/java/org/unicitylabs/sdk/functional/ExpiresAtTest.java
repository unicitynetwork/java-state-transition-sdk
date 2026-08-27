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
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.transaction.MintTransaction;
import org.unicitylabs.sdk.transaction.TokenSalt;
import org.unicitylabs.sdk.transaction.TokenType;
import org.unicitylabs.sdk.utils.ExpiresAt;

/**
 * The Unicity Service admits a request only in a round whose reference time is strictly below the
 * request's deadline. A request that carries no deadline is admitted under a service-assigned one,
 * which is not recorded and is not re-checked by a verifier.
 */
public class ExpiresAtTest {

  private final TestAggregatorClient aggregatorClient = TestAggregatorClient.create();
  private final StateTransitionClient client = new StateTransitionClient(this.aggregatorClient);
  private final SignaturePredicate recipient =
          SignaturePredicate.fromSigningService(SigningService.generate());

  private CertificationStatus submit(Long expiresAt) throws Exception {
    MintTransaction transaction = MintTransaction.builder(NetworkId.LOCAL, this.recipient)
            .expiresAt(expiresAt)
            .build();

    return this.client.submitCertificationRequest(
            CertificationData.fromMintTransaction(transaction)).get().getStatus();
  }

  @Test
  public void acceptsARequestWhoseDeadlineIsAheadOfTheRoundReferenceTime() throws Exception {
    Assertions.assertEquals(CertificationStatus.SUCCESS,
            this.submit(ExpiresAt.expiresAt()));
  }

  @Test
  public void rejectsARequestWhoseDeadlineTheRoundReferenceTimeHasReached() throws Exception {
    Assertions.assertEquals(CertificationStatus.REQUEST_EXPIRED,
            this.submit(ExpiresAt.expiredExpiresAt()));
  }

  @Test
  public void bindsTheDeadlineIntoTheTransactionHash() {
    TokenType tokenType = TokenType.generate();
    TokenSalt salt = TokenSalt.generate();

    MintTransaction first = MintTransaction.builder(NetworkId.LOCAL, this.recipient)
            .tokenType(tokenType)
            .salt(salt)
            .expiresAt(1755000000L)
            .build();
    MintTransaction second = MintTransaction.builder(NetworkId.LOCAL, this.recipient)
            .tokenType(tokenType)
            .salt(salt)
            .expiresAt(1755000001L)
            .build();

    Assertions.assertNotEquals(first.calculateTransactionHash(),
            second.calculateTransactionHash());
  }

  @Test
  public void omittingTheDeadlineNeedsNoClockAndKeepsTheSameWireShape() {
    MintTransaction transaction = MintTransaction.builder(NetworkId.LOCAL, this.recipient).build();
    MintTransaction decoded = MintTransaction.fromCbor(transaction.toCbor());
    CertificationData certificationData = CertificationData.fromMintTransaction(transaction);

    Assertions.assertFalse(transaction.getExpiresAt().isPresent());
    Assertions.assertFalse(decoded.getExpiresAt().isPresent());
    Assertions.assertArrayEquals(transaction.toCbor(), decoded.toCbor());
    Assertions.assertFalse(certificationData.getExpiresAt().isPresent());

    // The absent deadline holds its slot as CBOR null rather than shortening the
    // array, so both profiles are the same version with the same field count.
    Assertions.assertEquals(MintTransaction.VERSION, transaction.toCbor()[4]);
    Assertions.assertEquals(CertificationData.VERSION, certificationData.toCbor()[4]);
  }

  @Test
  public void rejectsAnyVersionOtherThanTheCurrentOne() {
    MintTransaction transaction = MintTransaction.builder(NetworkId.LOCAL, this.recipient)
            .expiresAt(1_755_000_000L)
            .build();

    for (byte badVersion : new byte[] {1, 3}) {
      byte[] mismatched = transaction.toCbor();
      Assertions.assertEquals(2, mismatched[4], "fixture version offset");
      mismatched[4] = badVersion;

      Assertions.assertThrows(
              CborSerializationException.class,
              () -> MintTransaction.fromCbor(mismatched)
      );
    }
  }
}
