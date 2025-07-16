package com.unicity.sdk.api;

import java.util.Objects;

public class BlockHeightResponse {

  private final long blockNumber;

  public BlockHeightResponse(long blockNumber) {
    this.blockNumber = blockNumber;
  }

  public long getBlockNumber() {
    return this.blockNumber;
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof BlockHeightResponse)) {
      return false;
    }
    BlockHeightResponse that = (BlockHeightResponse) o;
    return this.blockNumber == that.blockNumber;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.blockNumber);
  }
}
