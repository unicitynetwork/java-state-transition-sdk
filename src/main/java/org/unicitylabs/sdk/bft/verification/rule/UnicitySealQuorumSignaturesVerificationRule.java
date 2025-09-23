package org.unicitylabs.sdk.bft.verification.rule;

import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
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
    byte[] unicitySealBytes = UnicitySealQuorumSignaturesVerificationRule.encodeUnicitySeal(
        unicitySeal
    );
    if (unicitySealBytes == null) {
      return VerificationResult.fail("Could not encode UnicitySeal.");
    }

    RootTrustBase trustBase = context.getTrustBase();

    List<VerificationResult> results = new ArrayList<>();
    DataHash hash = new DataHasher(HashAlgorithm.SHA256).update(unicitySealBytes).digest();
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
      return VerificationResult.success();
    }

    return VerificationResult.fail("Quorum threshold not reached.");
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

  private static byte[] encodeUnicitySeal(UnicitySeal seal) {
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      CBORFactory factory = (CBORFactory) UnicityObjectMapper.CBOR.getFactory();
      CBORGenerator gen = factory.createGenerator(out);

      gen.writeTag(1001);
      gen.writeStartArray(seal, 8);
      gen.writeObject(seal.getVersion());
      gen.writeObject(seal.getNetworkId());
      gen.writeObject(seal.getRootChainRoundNumber());
      gen.writeObject(seal.getEpoch());
      gen.writeObject(seal.getTimestamp());
      gen.writeObject(seal.getPreviousHash());
      gen.writeObject(seal.getHash());
      gen.writeObject(null);
      gen.writeEndArray();

      gen.close();
      return out.toByteArray();
    } catch (IOException e) {
      return null;
    }
  }

}
