package org.unicitylabs.sdk.jsonrpc;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.unicitylabs.sdk.api.BlockHeightResponse;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class JsonRpcResponseTest {

  @Test
  public void testJsonSerialization() throws JsonProcessingException {
    JsonRpcResponse<BlockHeightResponse> data = UnicityObjectMapper.JSON.readValue(
        "{\"jsonrpc\":\"2.0\",\"result\":{\"blockNumber\":\"846973\"},\"id\":\"60ce8f4d-4c78-4690-a330-a92d3cf497a9\"}",
        UnicityObjectMapper.JSON.getTypeFactory()
            .constructParametricType(JsonRpcResponse.class, BlockHeightResponse.class));

    Assertions.assertEquals(new JsonRpcResponse<BlockHeightResponse>("2.0", new BlockHeightResponse(846973L), null, UUID.fromString("60ce8f4d-4c78-4690-a330-a92d3cf497a9")), data);
  }

}
