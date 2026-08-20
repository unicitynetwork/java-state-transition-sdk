package org.unicitylabs.sdk.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.predicate.builtin.SignaturePredicate;
import org.unicitylabs.sdk.transaction.MintTransaction;
import org.unicitylabs.sdk.transaction.TokenSalt;
import org.unicitylabs.sdk.transaction.TokenType;
import org.unicitylabs.sdk.util.HexConverter;

/**
 * Pins the certification-data encoding against the vectors the TypeScript and Rust SDKs and the
 * aggregator assert on. The two profiles must be byte-identical across implementations, and the
 * v1 profile must reproduce the bytes produced before request timeouts existed.
 */
public class CrossSdkEncodingTest {

  private static final byte[] PUBLIC_KEY =
          HexConverter.decode("02ce9f22e51333c97a8fb1f807a229ece3a8765a16af5fc1a13e30834be3280026");
  private static final long TIMEOUT = 1755000000L;

  private static final String V1 =
          "d998778501d9987883014101582103a19eef04b8856f50bf2d688b0d8804575115e53d2a7780da3636283"
                  + "43f9635075820e4b183ff6b7a399983cee26e4feea85d517dede0142def5c838e593a9e615241"
                  + "5820df524cffc08a1dc30579a8a51f440a97b30630988084f8d12a4d8bd741c7791258419efb6"
                  + "37f14dbdaada6e293e2182932d82265b04b1abf4f28bc4c285b32b5e2325140fe7f94bc9b705c"
                  + "568b4fcb7f9ea90cf0fadcacc1b4504275f81558aad1e700";
  private static final String V2 =
          "d998778602d9987883014101582103a19eef04b8856f50bf2d688b0d8804575115e53d2a7780da3636283"
                  + "43f9635075820e4b183ff6b7a399983cee26e4feea85d517dede0142def5c838e593a9e615241"
                  + "5820ed275ff0a0694d1b61ec22f13914a431569220ba7f2f043d7940aac78d02c2f91a689b2cc"
                  + "0584111f0f7929d70e0e32db9159b7e23b6e0043502bc36609728e9dc0353251c241a7b1adb04"
                  + "7c9234cd77ed519c409048a6c8bc247f0262c1f161b03d6fee49426e00";

  private static MintTransaction mint(long timeout) {
    SignaturePredicate recipient = SignaturePredicate.create(PUBLIC_KEY);
    TokenType tokenType = new TokenType(new byte[32]);
    TokenSalt salt = TokenSalt.fromBytes(new byte[32]);

    return timeout == 0
            ? MintTransaction.create(NetworkId.MAINNET, recipient, (byte[]) null, tokenType, salt)
            : MintTransaction.create(NetworkId.MAINNET, recipient, timeout, null, tokenType, salt);
  }

  @Test
  public void serviceDefaultProfileMatchesTheSharedV1Vector() {
    MintTransaction transaction = mint(0);
    CertificationData certificationData = CertificationData.fromMintTransaction(transaction);

    Assertions.assertEquals(0, transaction.getTimeout());
    Assertions.assertEquals(V1, HexConverter.encode(certificationData.toCbor()));
  }

  @Test
  public void explicitTimeoutProfileMatchesTheSharedV2Vector() {
    MintTransaction transaction = mint(TIMEOUT);
    CertificationData certificationData = CertificationData.fromMintTransaction(transaction);

    Assertions.assertEquals(TIMEOUT, transaction.getTimeout());
    Assertions.assertEquals(V2, HexConverter.encode(certificationData.toCbor()));
  }
}
