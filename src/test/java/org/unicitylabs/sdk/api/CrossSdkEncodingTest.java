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
 * aggregator assert on. The bytes must be identical across implementations, both when the sender
 * chooses a deadline and when it leaves the choice to the Unicity Service.
 */
public class CrossSdkEncodingTest {

  private static final byte[] PUBLIC_KEY =
          HexConverter.decode("02ce9f22e51333c97a8fb1f807a229ece3a8765a16af5fc1a13e30834be3280026");
  private static final long EXPIRES_AT = 1755000000L;

  private static final String EXPLICIT =
          "d998778602d9987883014101582103a19eef04b8856f50bf2d688b0d8804575115e53d2a7780da3636283"
                  + "43f9635075820e4b183ff6b7a399983cee26e4feea85d517dede0142def5c838e593a9e615241"
                  + "5820ed275ff0a0694d1b61ec22f13914a431569220ba7f2f043d7940aac78d02c2f91a689b2cc"
                  + "0584111f0f7929d70e0e32db9159b7e23b6e0043502bc36609728e9dc0353251c241a7b1adb04"
                  + "7c9234cd77ed519c409048a6c8bc247f0262c1f161b03d6fee49426e00";
  private static final String ABSENT =
          "d998778602d9987883014101582103a19eef04b8856f50bf2d688b0d8804575115e53d2a7780da3636283"
                  + "43f9635075820e4b183ff6b7a399983cee26e4feea85d517dede0142def5c838e593a9e615241"
                  + "5820c034e096d7bdf71ba759558663b5cafb7279ecb7e284443e5e6cbce0461aceeef6584154"
                  + "ca6b19a7dbcae7a6adc38af5c8672f81943ecaf51345436684299b4b7ac81a57db2653f32048"
                  + "981e37913db4749ca08d998d1fac4a52ab5579988bc2c50de900";

  private static MintTransaction mint(Long expiresAt) {
    return MintTransaction
            .builder(NetworkId.MAINNET, SignaturePredicate.create(PUBLIC_KEY))
            .tokenType(new TokenType(new byte[32]))
            .salt(TokenSalt.fromBytes(new byte[32]))
            .expiresAt(expiresAt)
            .build();
  }

  @Test
  public void explicitDeadlineMatchesTheSharedVector() {
    MintTransaction transaction = mint(EXPIRES_AT);
    CertificationData certificationData = CertificationData.fromMintTransaction(transaction);

    Assertions.assertEquals(EXPIRES_AT, transaction.getExpiresAt().orElseThrow(AssertionError::new));
    Assertions.assertEquals(EXPLICIT, HexConverter.encode(certificationData.toCbor()));
  }

  @Test
  public void absentDeadlineMatchesTheSharedVector() {
    MintTransaction transaction = mint(null);
    CertificationData certificationData = CertificationData.fromMintTransaction(transaction);

    Assertions.assertFalse(transaction.getExpiresAt().isPresent());
    Assertions.assertEquals(ABSENT, HexConverter.encode(certificationData.toCbor()));
  }
}
