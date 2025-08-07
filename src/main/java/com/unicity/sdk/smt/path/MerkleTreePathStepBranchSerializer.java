package com.unicity.sdk.smt.path;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.unicity.sdk.util.HexConverter;

import java.io.IOException;

class MerkleTreePathStepBranchSerializer extends JsonSerializer<MerkleTreePathStepBranch> {

  @Override
  public void serialize(MerkleTreePathStepBranch value, JsonGenerator gen,
      SerializerProvider serializers) throws IOException {
    if (value == null) {
      gen.writeNull();
    } else {
      String[] result = value.getValue() == null ? new String[0]
          : new String[]{HexConverter.encode(value.getValue())};
      gen.writeArray(result, 0, result.length);
    }
  }
}
