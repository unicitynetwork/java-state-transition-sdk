package org.unicitylabs.sdk.transaction.verification;

import org.unicitylabs.sdk.predicate.EncodedPredicate;
import org.unicitylabs.sdk.predicate.builtin.SignaturePredicate;
import org.unicitylabs.sdk.unicityid.CertifiedUnicityIdMintTransaction;
import org.unicitylabs.sdk.util.verification.VerificationResult;
import org.unicitylabs.sdk.util.verification.VerificationStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Verification rule for the genesis (mint) of a unicity id token. Validates the inclusion proof of
 * the certified mint transaction, and optionally checks that the genesis lock script matches an
 * expected issuer public key.
 */
public class CertifiedUnicityIdMintTransactionVerificationRule {

  private CertifiedUnicityIdMintTransactionVerificationRule() {
  }

  /**
   * Verify the certified unicity id mint transaction.
   *
   * @param genesis certified unicity id mint transaction to verify
   * @param context shared verification context (trust base + registries)
   * @param issuerPublicKey expected issuer public key, or {@code null} to skip the lock-script
   *     issuer check (e.g., when minting a fresh token where no external issuer is being asserted)
   *
   * @return verification result
   */
  public static VerificationResult<VerificationStatus> verify(
          CertifiedUnicityIdMintTransaction genesis,
          VerificationContext context,
          byte[] issuerPublicKey
  ) {
    List<VerificationResult<?>> results = new ArrayList<>();

    if (!genesis.getNetworkId().equals(context.getTrustBase().getNetworkId())) {
      results.add(new VerificationResult<>("MintNetworkMatchesTrustBaseRule",
              VerificationStatus.FAIL));
      return new VerificationResult<>(
              "CertifiedUnicityIdMintTransactionVerificationRule",
              VerificationStatus.FAIL,
              "Mint network does not match trust base.",
              results
      );
    }
    results.add(new VerificationResult<>("MintNetworkMatchesTrustBaseRule",
            VerificationStatus.OK));

    if (issuerPublicKey != null) {
      EncodedPredicate expectedLockScript = EncodedPredicate.fromPredicate(
              SignaturePredicate.create(issuerPublicKey));
      if (!expectedLockScript.equals(genesis.getLockScript())) {
        results.add(new VerificationResult<>("IsLockScriptValidVerificationRule",
                VerificationStatus.FAIL));
        return new VerificationResult<>(
                "CertifiedUnicityIdMintTransactionVerificationRule",
                VerificationStatus.FAIL,
                "Lock script does not match expected unicity-id issuer.",
                results
        );
      }
      results.add(new VerificationResult<>("IsLockScriptValidVerificationRule",
              VerificationStatus.OK));
    }

    VerificationResult<InclusionProofVerificationStatus> result = InclusionProofVerificationRule.verify(
            context.getTrustBase(),
            context.getPredicateVerifier(),
            genesis.getInclusionProof(),
            genesis
    );
    results.add(result);
    if (result.getStatus() != InclusionProofVerificationStatus.OK) {
      return new VerificationResult<>(
              "CertifiedUnicityIdMintTransactionVerificationRule",
              VerificationStatus.FAIL,
              String.format("Inclusion proof verification failed: %s", result.getStatus()),
              results
      );
    }

    return new VerificationResult<>(
            "CertifiedUnicityIdMintTransactionVerificationRule",
            VerificationStatus.OK,
            "",
            results
    );
  }
}
