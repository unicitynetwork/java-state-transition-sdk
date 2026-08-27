package org.unicitylabs.sdk.interop;

import java.nio.file.Files;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.unicitylabs.sdk.api.bft.RootTrustBase;
import org.unicitylabs.sdk.predicate.verification.PredicateVerifierService;
import org.unicitylabs.sdk.transaction.Token;
import org.unicitylabs.sdk.transaction.verification.MintJustificationVerifierService;
import org.unicitylabs.sdk.transaction.verification.TokenIssuanceVerifierService;
import org.unicitylabs.sdk.transaction.verification.VerificationContext;
import org.unicitylabs.sdk.util.verification.VerificationStatus;

/**
 * The consuming half: a token minted and transferred by the TypeScript SDK, decoded and fully
 * verified here.
 *
 * <p>This is the test that catches a container-format divergence. Golden byte vectors for
 * {@code CertificationData} — which both SDKs already have — pin the structure sent to the
 * aggregator, and they stayed byte-identical through the whole of the change that broke tokens.
 * Only carrying a real token across the language boundary exercises {@code Token}, the certified
 * transactions inside it, and the verification semantics that read them.
 */
class JsProducedTokenTest {

  private static final String TOKEN = "js-token-v2.cbor";
  private static final String TRUST_BASE = "js-token-v2.trust-base.json";

  @Test
  void verifiesATokenProducedByTheTypeScriptSdk() throws Exception {
    Assumptions.assumeTrue(Files.exists(InteropFixture.VECTORS.resolve(TOKEN)),
            "TypeScript interop vector not present");

    Token token = Token.fromCbor(InteropFixture.read(TOKEN));
    RootTrustBase trustBase = RootTrustBase.fromJson(InteropFixture.readText(TRUST_BASE));

    VerificationContext context = new VerificationContext(
            trustBase,
            PredicateVerifierService.create(),
            new MintJustificationVerifierService(),
            new TokenIssuanceVerifierService(false));

    Assertions.assertEquals(VerificationStatus.OK, token.verify(context).getStatus(),
            "a token the TypeScript SDK produced must verify here unchanged");

    // The deadline is committed by the transaction hash, so it has to survive the crossing.
    Assertions.assertEquals(InteropFixture.EXPIRES_AT,
            token.getGenesis().getExpiresAt().orElseThrow(AssertionError::new));
    Assertions.assertEquals(InteropFixture.REFERENCE_TIME,
            token.getGenesis().getReferenceTime());
    Assertions.assertEquals(1, token.getTransactions().size());
  }
}
