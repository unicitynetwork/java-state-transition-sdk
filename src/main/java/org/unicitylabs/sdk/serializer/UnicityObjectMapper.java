package org.unicitylabs.sdk.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.unicitylabs.sdk.serializer.json.ByteArrayJson;
import org.unicitylabs.sdk.serializer.json.SignatureJson;
import org.unicitylabs.sdk.signing.Signature;


public class UnicityObjectMapper {
  public static final ObjectMapper JSON = createJsonObjectMapper();

  private static ObjectMapper createJsonObjectMapper() {
    SimpleModule module = new SimpleModule();
    module.addSerializer(byte[].class, new ByteArrayJson.Serializer());
    module.addDeserializer(byte[].class, new ByteArrayJson.Deserializer());
    module.addSerializer(Signature.class, new SignatureJson.Serializer());
    module.addDeserializer(Signature.class, new SignatureJson.Deserializer());

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new Jdk8Module());
    objectMapper.registerModule(module);
    return objectMapper;
  }
}