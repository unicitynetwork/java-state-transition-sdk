
package org.unicitylabs.sdk.transaction.split;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Objects;
import org.unicitylabs.sdk.mtree.plain.SparseMerkleTreePath;
import org.unicitylabs.sdk.mtree.sum.SparseMerkleSumTreePath;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.token.fungible.CoinId;

/**
 * Split mint reason proof for specific coin.
 */
public class SplitMintReasonProof {

  private final CoinId coinId;
  private final SparseMerkleTreePath aggregationPath;
  private final SparseMerkleSumTreePath coinTreePath;

  @JsonCreator
  SplitMintReasonProof(
      @JsonProperty("coinId") CoinId coinId,
      @JsonProperty("aggregationPath") SparseMerkleTreePath aggregationPath,
      @JsonProperty("coinTreePath") SparseMerkleSumTreePath coinTreePath
  ) {
    Objects.requireNonNull(coinId, "coinId cannot be null");
    Objects.requireNonNull(aggregationPath, "aggregationPath cannot be null");
    Objects.requireNonNull(coinTreePath, "coinTreePath cannot be null");

    this.coinId = coinId;
    this.aggregationPath = aggregationPath;
    this.coinTreePath = coinTreePath;
  }

  /**
   * Get coin ID associated with proof.
   *
   * @return coin id
   */
  public CoinId getCoinId() {
    return this.coinId;
  }

  /**
   * Get aggregation path for current coin in coin trees.
   *
   * @return aggregation path
   */
  public SparseMerkleTreePath getAggregationPath() {
    return this.aggregationPath;
  }

  /**
   * Get coin tree path for current coin.
   *
   * @return coin tree path
   */
  public SparseMerkleSumTreePath getCoinTreePath() {
    return this.coinTreePath;
  }

  /**
   * Create split mint reason proof from CBOR bytes.
   *
   * @param bytes CBOR bytes
   * @return split mint reason proof
   */
  public static SplitMintReasonProof fromCbor(byte[] bytes) {
    List<byte[]> data = CborDeserializer.readArray(bytes);

    return new SplitMintReasonProof(
        new CoinId(CborDeserializer.readByteString(data.get(0))),
        SparseMerkleTreePath.fromCbor(data.get(1)),
        SparseMerkleSumTreePath.fromCbor(data.get(2))
    );
  }

  /**
   * Convert split mint reason proof to CBOR bytes.
   *
   * @return CBOR bytes
   */
  public byte[] toCbor() {
    return CborSerializer.encodeArray(
        CborSerializer.encodeByteString(this.coinId.getBytes()),
        this.aggregationPath.toCbor(),
        this.coinTreePath.toCbor()
    );
  }
}
