package org.unicitylabs.sdk.common;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.crypto.secp256k1.SigningService;
import org.unicitylabs.sdk.predicate.builtin.SignaturePredicate;
import org.unicitylabs.sdk.transaction.Token;
import org.unicitylabs.sdk.transaction.verification.VerificationContext;
import org.unicitylabs.sdk.util.verification.VerificationStatus;
import org.unicitylabs.sdk.utils.TokenUtils;

/**
 * Common test flows for token operations, matching TypeScript SDK's CommonTestFlow.
 */
public abstract class CommonTestFlow {

  protected StateTransitionClient client;
  protected VerificationContext context;

  private static final SigningService ALICE_SIGNING_SERVICE = SigningService.generate();
  private static final SigningService BOB_SIGNING_SERVICE = SigningService.generate();
  private static final SigningService CAROL_SIGNING_SERVICE = SigningService.generate();


  /**
   * Test basic token transfer flow: Alice -> Bob -> Carol
   */
  @Test
  public void testTransferFlow() throws Exception {
    Token aliceToken = TokenUtils.mintToken(
            this.client,
            this.context,
            SignaturePredicate.create(ALICE_SIGNING_SERVICE.getPublicKey())
    );

    Token bobToken = TokenUtils.transferToken(
            this.client,
            this.context,
            aliceToken.toCbor(),
            SignaturePredicate.create(BOB_SIGNING_SERVICE.getPublicKey()),
            ALICE_SIGNING_SERVICE
    );

    Token carolToken = TokenUtils.transferToken(
            this.client,
            this.context,
            bobToken.toCbor(),
            SignaturePredicate.create(CAROL_SIGNING_SERVICE.getPublicKey()),
            BOB_SIGNING_SERVICE
    );

    Assertions.assertEquals(VerificationStatus.OK,
            carolToken.verify(this.context).getStatus());
  }
}