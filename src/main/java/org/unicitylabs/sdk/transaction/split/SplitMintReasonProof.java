
package org.unicitylabs.sdk.transaction.split;

import java.util.List;
import java.util.Objects;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;
import org.unicitylabs.sdk.mtree.sum.SparseMerkleSumTreePath;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.token.fungible.CoinId;

public class SplitMintReasonProof {

  private final CoinId coinId;
  private final SparseMerkleTreePath aggregationPath;
  private final SparseMerkleSumTreePath coinTreePath;

  public SplitMintReasonProof(
      CoinId coinId,
      SparseMerkleTreePath aggregationPath, SparseMerkleSumTreePath coinTreePath) {
    Objects.requireNonNull(coinId, "coinId cannot be null");
    Objects.requireNonNull(aggregationPath, "aggregationPath cannot be null");
    Objects.requireNonNull(coinTreePath, "coinTreePath cannot be null");

    this.coinId = coinId;
    this.aggregationPath = aggregationPath;
    this.coinTreePath = coinTreePath;
  }

  public CoinId getCoinId() {
    return this.coinId;
  }

  public SparseMerkleTreePath getAggregationPath() {
    return this.aggregationPath;
  }

  public SparseMerkleSumTreePath getCoinTreePath() {
    return this.coinTreePath;
  }

  public static SplitMintReasonProof fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new SplitMintReasonProof(
        new CoinId(CborDeserializer.readByteString(data.get(0))),
        SparseMerkleTreePath.fromCbor(data.get(1)),
        SparseMerkleSumTreePath.fromCbor(data.get(2))
    );
  }

  public byte[] toCbor() {
    return CborSerializer.encodeArray(
        CborSerializer.encodeByteString(this.coinId.getBytes()),
        this.aggregationPath.toCbor(),
        this.coinTreePath.toCbor()
    );
  }
}
