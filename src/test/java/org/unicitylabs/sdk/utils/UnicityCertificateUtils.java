package org.unicitylabs.sdk.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Map;
import org.unicitylabs.sdk.bft.InputRecord;
import org.unicitylabs.sdk.bft.ShardTreeCertificate;
import org.unicitylabs.sdk.bft.UnicityCertificate;
import org.unicitylabs.sdk.bft.UnicitySeal;
import org.unicitylabs.sdk.bft.UnicityTreeCertificate;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.DataHasher;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.signing.SigningService;

public class UnicityCertificateUtils {

  public static UnicityCertificate generateCertificate(
      SigningService signingService,
      DataHash rootHash
  ) {
    try {
      InputRecord inputRecord = new InputRecord(0, 0, 0, null, rootHash.getImprint(), new byte[10],
          0,
          new byte[10], 0, new byte[10]);
      UnicityTreeCertificate unicityTreeCertificate = new UnicityTreeCertificate(0, 0, List.of());
      byte[] technicalRecordHash = new byte[32];
      byte[] shardConfigurationHash = new byte[32];
      ShardTreeCertificate shardTreeCertificate = new ShardTreeCertificate(
          new byte[32], List.of()
      );

      DataHash shardTreeCertificateRootHash = UnicityCertificate.calculateShardTreeCertificateRootHash(
          inputRecord,
          technicalRecordHash,
          shardConfigurationHash,
          shardTreeCertificate
      );

      byte[] key = ByteBuffer.allocate(4)
          .order(ByteOrder.BIG_ENDIAN)
          .putInt(unicityTreeCertificate.getPartitionIdentifier())
          .array();

      DataHash unicitySealHash = new DataHasher(HashAlgorithm.SHA256)
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

      UnicitySeal seal = new UnicitySeal(
          0,
          (short) 0,
          0L,
          0L,
          0L,
          null,
          unicitySealHash.getData(),
          null
      );

      return new UnicityCertificate(
          0,
          new InputRecord(0, 0, 0, null, rootHash.getImprint(), new byte[10], 0,
              new byte[10], 0, new byte[10]),
          technicalRecordHash,
          shardConfigurationHash,
          shardTreeCertificate,
          new UnicityTreeCertificate(0, 0, List.of()),
          seal.withSignatures(
              Map.of(
                  "NODE",
                  signingService.sign(
                      new DataHasher(HashAlgorithm.SHA256).update(seal.encode()).digest()
                  ).encode()
              )
          )
      );
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate UnicityCertificate", e);
    }
  }
}
