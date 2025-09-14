package org.unicitylabs.sdk.bft;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.unicitylabs.sdk.util.HexConverter;

public class ShardTreeCertificate {

  private final byte[] shard;
  private final List<byte[]> siblingHashList;

  public ShardTreeCertificate(byte[] shard, List<byte[]> siblingHashList) {
    Objects.requireNonNull(shard, "Shard cannot be null");
    Objects.requireNonNull(siblingHashList, "Sibling hash list cannot be null");

    this.shard = shard;
    this.siblingHashList = siblingHashList;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof ShardTreeCertificate)) {
      return false;
    }
    ShardTreeCertificate that = (ShardTreeCertificate) o;
    return Objects.deepEquals(this.shard, that.shard) && Objects.equals(
        this.siblingHashList, that.siblingHashList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(Arrays.hashCode(this.shard), this.siblingHashList);
  }

  @Override
  public String toString() {
    return String.format("ShardTreeCertificate{shard=%s, siblingHashList=%s}",
        HexConverter.encode(this.shard), this.siblingHashList);
  }
}
