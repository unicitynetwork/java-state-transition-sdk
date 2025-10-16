package org.unicitylabs.sdk.serializer.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.math.BigInteger;

/**
 * Serializes a long value as a JSON string.
 */
public class BigIntegerAsStringSerializer extends JsonSerializer<BigInteger> {

  @Override
  public void serialize(BigInteger value, JsonGenerator gen, SerializerProvider serializers)
      throws IOException {
    gen.writeString(value.toString());
  }
}

