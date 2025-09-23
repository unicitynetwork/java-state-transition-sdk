package org.unicitylabs.sdk.bft.verification.rule;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.List;
import org.unicitylabs.sdk.bft.UnicityCertificate;
import org.unicitylabs.sdk.bft.UnicityTreeCertificate;
import org.unicitylabs.sdk.bft.verification.UnicityCertificateVerificationContext;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.verification.VerificationResult;
import org.unicitylabs.sdk.verification.VerificationRule;

public class UnicitySealHashMatchesWithRootHashRule extends
    VerificationRule<UnicityCertificateVerificationContext> {

  public UnicitySealHashMatchesWithRootHashRule() {
    this(null, null);
  }

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
    DataHash shardTreeCertificateRootHash = UnicitySealHashMatchesWithRootHashRule
        .calculateShardTreeCertificateRootHash(context.getUnicityCertificate());

    if (shardTreeCertificateRootHash == null) {
      return VerificationResult.fail("Could not calculate shard tree certificate root hash.");
    }

    UnicityTreeCertificate unicityTreeCertificate = context.getUnicityCertificate()
        .getUnicityTreeCertificate();
    byte[] key = ByteBuffer.allocate(4)
        .order(ByteOrder.BIG_ENDIAN)
        .putInt(unicityTreeCertificate.getPartitionIdentifier())
        .array();

    try {
      DataHash result = new DataHasher(HashAlgorithm.SHA256)
          .update(UnicityObjectMapper.CBOR.writeValueAsBytes(new byte[]{(byte) 0x01})) // LEAF
          .update(UnicityObjectMapper.CBOR.writeValueAsBytes(key))
          .update(
              UnicityObjectMapper.CBOR.writeValueAsBytes(
                  new DataHasher(HashAlgorithm.SHA256)
                      .update(
                          UnicityObjectMapper.CBOR.writeValueAsBytes(
                              shardTreeCertificateRootHash.getData()
                          )
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
            .update(UnicityObjectMapper.CBOR.writeValueAsBytes(new byte[]{(byte) 0x00})) // NODE
            .update(UnicityObjectMapper.CBOR.writeValueAsBytes(stepKey));

        if (Arrays.compare(key, stepKey) > 0) {
          hasher
              .update(UnicityObjectMapper.CBOR.writeValueAsBytes(step.getHash()))
              .update(UnicityObjectMapper.CBOR.writeValueAsBytes(result.getData()));
        } else {
          hasher
              .update(UnicityObjectMapper.CBOR.writeValueAsBytes(result.getData()))
              .update(UnicityObjectMapper.CBOR.writeValueAsBytes(step.getHash()));
        }

        result = hasher.digest();
      }

      byte[] unicitySealHash = context.getUnicityCertificate().getUnicitySeal().getHash();

      if (Arrays.compare(unicitySealHash, result.getData()) != 0) {
        return VerificationResult.fail("Unicity seal hash does not match tree root.");
      }
    } catch (IOException e) {
      // TODO: Fix message
      return VerificationResult.fail(e.getMessage());
    }

    return VerificationResult.success();
  }

  private static DataHash calculateShardTreeCertificateRootHash(
      UnicityCertificate unicityCertificate) {
    try {
      DataHash rootHash = new DataHasher(HashAlgorithm.SHA256)
          .update(UnicityObjectMapper.CBOR.writeValueAsBytes(unicityCertificate.getInputRecord()))
          .update(UnicityObjectMapper.CBOR.writeValueAsBytes(
              unicityCertificate.getTechnicalRecordHash()))
          .update(UnicityObjectMapper.CBOR.writeValueAsBytes(
              unicityCertificate.getShardConfigurationHash()))
          .digest();

      byte[] shardId = unicityCertificate.getShardTreeCertificate().getShard();
      List<byte[]> siblingHashes = unicityCertificate.getShardTreeCertificate()
          .getSiblingHashList();
      for (int i = 0; i < siblingHashes.size(); i++) {
        boolean isRight = shardId[(shardId.length - 1) - (i / 8)] == 1;
        if (isRight) {
          rootHash = new DataHasher(HashAlgorithm.SHA256)
              .update(siblingHashes.get(i))
              .update(rootHash.getData())
              .digest();
        } else {
          rootHash = new DataHasher(HashAlgorithm.SHA256)
              .update(rootHash.getData())
              .update(siblingHashes.get(i))
              .digest();
        }
      }

      return rootHash;
    } catch (Exception e) {
      return null;
    }
  }
}
