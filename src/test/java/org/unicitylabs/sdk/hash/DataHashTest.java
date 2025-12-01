package org.unicitylabs.sdk.hash;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.serializer.json.JsonSerializationException;

public class DataHashTest {

  @Test
  public void testInvalidDataHashArguments() {
    NullPointerException exception = Assertions.assertThrows(NullPointerException.class,
        () -> new DataHash(null, new byte[32]));
    Assertions.assertEquals("algorithm cannot be null", exception.getMessage());
    exception = Assertions.assertThrows(NullPointerException.class,
        () -> new DataHash(HashAlgorithm.SHA256, null));
    Assertions.assertEquals("data cannot be null", exception.getMessage());
  }

  @Test
  public void testDataHashJsonSerialization() {
    Assertions.assertEquals(
        "\"00000000000000000000000000000000000000000000000000000000000000000000\"",

        new DataHash(HashAlgorithm.SHA256, new byte[32]).toJson()
    );
    Assertions.assertEquals(
        "\"000200000000000000000000000000000000\"",
        new DataHash(HashAlgorithm.SHA384, new byte[16]).toJson()
    );

    Assertions.assertEquals(
        new DataHash(HashAlgorithm.SHA256, new byte[32]),
        DataHash.fromJson("\"00000000000000000000000000000000000000000000000000000000000000000000\"")
    );
    Assertions.assertThrows(JsonSerializationException.class, () -> DataHash.fromJson("[]"));
    Assertions.assertThrows(JsonSerializationException.class, () -> DataHash.fromJson("\"AABBGG\""));
  }
}
