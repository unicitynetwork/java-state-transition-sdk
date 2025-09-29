package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.util.HexConverter;

public class AuthenticatorTest {

  @Test
  public void testJsonSerialization() throws JsonProcessingException {
    SigningService signingService = new SigningService(
        HexConverter.decode("0000000000000000000000000000000000000000000000000000000000000001"));
    Authenticator authenticator = Authenticator.create(
        signingService,
        DataHash.fromImprint(new byte[34]),
        DataHash.fromImprint(new byte[34])
    );

    Assertions.assertEquals(
        "8469736563703235366b3158210279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f817985841a0b37f8fba683cc68f6574cd43b39f0343a50008bf6ccea9d13231d9e7e2e1e411edc8d307254296264aebfc3dc76cd8b668373a072fd64665b50000e9fcce5201582200000000000000000000000000000000000000000000000000000000000000000000",
        HexConverter.encode(authenticator.toCbor()));

    Authenticator.fromJson("{\"algorithm\":\"secp256k1\",\"publicKey\":\"0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798\",\"signature\":\"a0b37f8fba683cc68f6574cd43b39f0343a50008bf6ccea9d13231d9e7e2e1e411edc8d307254296264aebfc3dc76cd8b668373a072fd64665b50000e9fcce5201\",\"stateHash\":\"00000000000000000000000000000000000000000000000000000000000000000000\"}");
  }
}
