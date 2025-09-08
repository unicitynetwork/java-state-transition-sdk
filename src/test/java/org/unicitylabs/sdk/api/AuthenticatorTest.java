package org.unicitylabs.sdk.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.unicitylabs.sdk.hash.DataHash;
import org.unicitylabs.sdk.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.UnicityObjectMapper;
import org.unicitylabs.sdk.signing.Signature;
import org.unicitylabs.sdk.signing.SigningService;
import org.unicitylabs.sdk.util.HexConverter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AuthenticatorTest {

  @Test
  public void testJsonSerialization() throws JsonProcessingException {
    SigningService signingService = new SigningService(
        HexConverter.decode("0000000000000000000000000000000000000000000000000000000000000001"));
    Authenticator authenticator = new Authenticator(
        signingService.getAlgorithm(),
        signingService.getPublicKey(),
        Signature.decode(HexConverter.decode(
            "A0B37F8FBA683CC68F6574CD43B39F0343A50008BF6CCEA9D13231D9E7E2E1E411EDC8D307254296264AEBFC3DC76CD8B668373A072FD64665B50000E9FCCE5201")),
        new DataHash(HashAlgorithm.SHA256, new byte[32])
    );

    Assertions.assertEquals(
        "8469736563703235366b3158210279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f817985841a0b37f8fba683cc68f6574cd43b39f0343a50008bf6ccea9d13231d9e7e2e1e411edc8d307254296264aebfc3dc76cd8b668373a072fd64665b50000e9fcce5201582200000000000000000000000000000000000000000000000000000000000000000000",
        HexConverter.encode(UnicityObjectMapper.CBOR.writeValueAsBytes(authenticator)));

    UnicityObjectMapper.JSON.readValue(
        "{\"algorithm\":\"secp256k1\",\"publicKey\":\"0279be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798\",\"signature\":\"a0b37f8fba683cc68f6574cd43b39f0343a50008bf6ccea9d13231d9e7e2e1e411edc8d307254296264aebfc3dc76cd8b668373a072fd64665b50000e9fcce5201\",\"stateHash\":\"00000000000000000000000000000000000000000000000000000000000000000000\"}",
        Authenticator.class);
  }
}
