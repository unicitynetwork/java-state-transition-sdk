package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Objects;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;

public class BlockHeightResponse {

  private final long blockNumber;

  @JsonCreator
  private BlockHeightResponse(
      @JsonProperty("blockNumber") long blockNumber
  ) {
    this.blockNumber = blockNumber;
  }

  @JsonGetter("blockNumber")
  public long getBlockNumber() {
    return this.blockNumber;
  }

  public static BlockHeightResponse fromJson(String input) {
    try {
      return UnicityObjectMapper.JSON.readValue(input, BlockHeightResponse.class);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(BlockHeightResponse.class, e);
    }
  }

  public String toJson() {
    try {
      return UnicityObjectMapper.JSON.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      throw new JsonSerializationException(BlockHeightResponse.class, e);
    }
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
