package org.unicitylabs.sdk.bft.verification.rule;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import org.unicitylabs.sdk.bft.UnicityCertificate;
import org.unicitylabs.sdk.bft.UnicityTreeCertificate;
import org.unicitylabs.sdk.bft.verification.UnicityCertificateVerificationContext;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.verification.VerificationResult;
import org.unicitylabs.sdk.verification.VerificationRule;

/**
 * Rule to verify that the UnicitySeal hash matches the root hash of the UnicityTreeCertificate.
 */
public class UnicitySealHashMatchesWithRootHashRule extends
    VerificationRule<UnicityCertificateVerificationContext> {

  /**
   * Create the rule without any subsequent rules.
   */
  public UnicitySealHashMatchesWithRootHashRule() {
    this(null, null);
  }

  /**
   * Create the rule with subsequent rules for success and failure.
   *
   * @param onSuccessRule rule to execute on success
   * @param onFailureRule rule to execute on failure
   */
  public UnicitySealHashMatchesWithRootHashRule(
      VerificationRule<UnicityCertificateVerificationContext> onSuccessRule,
      VerificationRule<UnicityCertificateVerificationContext> onFailureRule
  ) {
    super(
        "Verifying UnicitySeal hash matches with tree root hash.",
        onSuccessRule,
        onFailureRule
    );
  }

  @Override
  public VerificationResult verify(UnicityCertificateVerificationContext context) {
    DataHash shardTreeCertificateRootHash = UnicityCertificate
        .calculateShardTreeCertificateRootHash(
            context.getUnicityCertificate().getInputRecord(),
            context.getUnicityCertificate().getTechnicalRecordHash(),
            context.getUnicityCertificate().getShardConfigurationHash(),
            context.getUnicityCertificate().getShardTreeCertificate()
        );

    if (shardTreeCertificateRootHash == null) {
      return VerificationResult.fail("Could not calculate shard tree certificate root hash.");
    }

    UnicityTreeCertificate unicityTreeCertificate = context.getUnicityCertificate()
        .getUnicityTreeCertificate();
    byte[] key = ByteBuffer.allocate(4)
        .order(ByteOrder.BIG_ENDIAN)
        .putInt(unicityTreeCertificate.getPartitionIdentifier())
        .array();

    DataHash result = new DataHasher(HashAlgorithm.SHA256)
        .update(CborSerializer.encodeByteString(new byte[]{(byte) 0x01})) // LEAF
        .update(CborSerializer.encodeByteString(key))
        .update(
            CborSerializer.encodeByteString(
                new DataHasher(HashAlgorithm.SHA256)
                    .update(
                        CborSerializer.encodeByteString(shardTreeCertificateRootHash.getData())
                    )
                    .digest()
                    .getData()
            )
        )
        .digest();

    for (UnicityTreeCertificate.HashStep step : unicityTreeCertificate.getSteps()) {
      byte[] stepKey = ByteBuffer.allocate(4)
          .order(ByteOrder.BIG_ENDIAN)
          .putInt(step.getKey())
          .array();

      DataHasher hasher = new DataHasher(HashAlgorithm.SHA256)
          .update(CborSerializer.encodeByteString(new byte[]{(byte) 0x00})) // NODE
          .update(CborSerializer.encodeByteString(stepKey));

      if (Arrays.compare(key, stepKey) > 0) {
        hasher
            .update(CborSerializer.encodeByteString(step.getHash()))
            .update(CborSerializer.encodeByteString(result.getData()));
      } else {
        hasher
            .update(CborSerializer.encodeByteString(result.getData()))
            .update(CborSerializer.encodeByteString(step.getHash()));
      }

      result = hasher.digest();
    }

    byte[] unicitySealHash = context.getUnicityCertificate().getUnicitySeal().getHash();

    if (Arrays.compare(unicitySealHash, result.getData()) != 0) {
      return VerificationResult.fail("Unicity seal hash does not match tree root.");
    }

    return VerificationResult.success();
  }
}
