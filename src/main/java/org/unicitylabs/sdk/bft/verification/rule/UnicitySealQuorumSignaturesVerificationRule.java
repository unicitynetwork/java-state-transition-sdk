package org.unicitylabs.sdk.bft.verification.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.unicitylabs.sdk.bft.RootTrustBase;
import org.unicitylabs.sdk.bft.UnicitySeal;
import org.unicitylabs.sdk.bft.verification.UnicityCertificateVerificationContext;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.verification.VerificationResult;
import org.unicitylabs.sdk.verification.VerificationRule;

public class UnicitySealQuorumSignaturesVerificationRule extends
    VerificationRule<UnicityCertificateVerificationContext> {

  public UnicitySealQuorumSignaturesVerificationRule() {
    this(null, null);
  }

  public UnicitySealQuorumSignaturesVerificationRule(
      VerificationRule<UnicityCertificateVerificationContext> onSuccessRule,
      VerificationRule<UnicityCertificateVerificationContext> onFailureRule
  ) {
    super(
        "Verifying UnicitySeal quorum signatures.",
        onSuccessRule,
        onFailureRule
    );
  }

  @Override
  public VerificationResult verify(UnicityCertificateVerificationContext context) {
    UnicitySeal unicitySeal = context.getUnicityCertificate().getUnicitySeal();
    RootTrustBase trustBase = context.getTrustBase();

    List<VerificationResult> results = new ArrayList<>();
    DataHash hash = new DataHasher(HashAlgorithm.SHA256)
        .update(unicitySeal.toCborWithoutSignatures())
        .digest();
    int successful = 0;
    for (Map.Entry<String, byte[]> entry : unicitySeal.getSignatures().entrySet()) {
      String nodeId = entry.getKey();
      byte[] signature = entry.getValue();

      VerificationResult result = UnicitySealQuorumSignaturesVerificationRule.verifySignature(
          trustBase.getRootNodes().stream()
              .filter(node -> node.getNodeId().equals(nodeId))
              .findFirst()
              .orElse(null),
          signature,
          hash.getData()
      );
      results.add(
          VerificationResult.fromChildren(
              String.format("Verifying node '%s' signature.", nodeId),
              List.of(result)
          )
      );

      if (result.isSuccessful()) {
        successful++;
      }
    }

    if (successful >= trustBase.getQuorumThreshold()) {
      return VerificationResult.success(results);
    }

    return VerificationResult.fail("Quorum threshold not reached.", results);
  }

  private static VerificationResult verifySignature(
      RootTrustBase.NodeInfo node,
      byte[] signature,
      byte[] hash
  ) {
    if (node == null) {
      return VerificationResult.fail("No root node defined.");
    }

    if (!SigningService.verifyWithPublicKey(
        hash,
        Arrays.copyOf(signature, signature.length - 1),
        node.getSigningKey()
    )) {
      return VerificationResult.fail(
          "Signature verification failed."
      );
    }

    return VerificationResult.success();
  }

}
