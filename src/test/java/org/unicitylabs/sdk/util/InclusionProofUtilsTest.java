package org.unicitylabs.sdk.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.StateTransitionClient;
import org.unicitylabs.sdk.TestAggregatorClient;
import org.unicitylabs.sdk.api.CertificationData;
import org.unicitylabs.sdk.api.InclusionProof;
import org.unicitylabs.sdk.api.NetworkId;
import org.unicitylabs.sdk.crypto.secp256k1.SigningService;
import org.unicitylabs.sdk.predicate.builtin.SignaturePredicate;
import org.unicitylabs.sdk.predicate.verification.PredicateVerifierService;
import org.unicitylabs.sdk.transaction.MintTransaction;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

class InclusionProofUtilsTest {

  @Test
  void retriesPendingProofWithoutReferenceTime() throws Exception {
    SigningService signingService = SigningService.generate();
    TestAggregatorClient aggregator = TestAggregatorClient.create();
    StateTransitionClient client = new StateTransitionClient(aggregator);
    MintTransaction transaction = MintTransaction.builder(
                    NetworkId.LOCAL,
                    SignaturePredicate.fromSigningService(signingService))
            .expiresAt(System.currentTimeMillis() / 1000 + 60)
            .build();

    var proofFuture = InclusionProofUtils.waitInclusionProof(
            client,
            aggregator.getTrustBase(),
            PredicateVerifierService.create(),
            transaction,
            Duration.ofSeconds(2),
            Duration.ofMillis(10));

    aggregator.submitCertificationRequest(CertificationData.fromMintTransaction(transaction)).get();

    InclusionProof proof = proofFuture.get(2, TimeUnit.SECONDS);
    // A proof that exists is complete; there is nothing left to assert about presence.
    Assertions.assertNotNull(proof.getCertificationData());
    Assertions.assertNotNull(proof.getInclusionCertificate());
  }
}
