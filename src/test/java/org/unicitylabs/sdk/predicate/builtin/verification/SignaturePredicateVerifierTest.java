package org.unicitylabs.sdk.predicate.builtin.verification;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.crypto.hash.DataHash;
import org.unicitylabs.sdk.crypto.hash.DataHasher;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.crypto.secp256k1.Signature;
import org.unicitylabs.sdk.crypto.secp256k1.SigningService;
import org.unicitylabs.sdk.predicate.EncodedPredicate;
import org.unicitylabs.sdk.predicate.builtin.SignaturePredicate;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;
import org.unicitylabs.sdk.util.verification.VerificationStatus;

public class SignaturePredicateVerifierTest {

  private final SignaturePredicateVerifier verifier = new SignaturePredicateVerifier();
  private final SigningService signingService = SigningService.generate();
  private final EncodedPredicate encodedPredicate = EncodedPredicate.fromPredicate(
          SignaturePredicate.fromSigningService(this.signingService));

  private static DataHash hash(byte[] data) {
    return new DataHasher(HashAlgorithm.SHA256).update(data).digest();
  }

  private final DataHash sourceStateHash = hash(new byte[]{1});
  private final DataHash transactionHash = hash(new byte[]{2});

  private Signature signUnlock() {
    DataHash digest = hash(
            CborSerializer.encodeArray(
                    CborSerializer.encodeByteString(this.sourceStateHash.getData()),
                    CborSerializer.encodeByteString(this.transactionHash.getData())
            )
    );
    return this.signingService.sign(digest);
  }

  @Test
  public void shouldAcceptValidUnlockScript() {
    Signature signature = signUnlock();

    Assertions.assertEquals(
            VerificationStatus.OK,
            this.verifier.verify(this.encodedPredicate, this.sourceStateHash, this.transactionHash,
                    signature.encode()).getStatus());
  }

  @Test
  public void shouldRejectTamperedRecoveryByte() {
    byte[] tampered = signUnlock().encode();
    tampered[64] ^= 1;

    Assertions.assertEquals(
            VerificationStatus.FAIL,
            this.verifier.verify(this.encodedPredicate, this.sourceStateHash, this.transactionHash,
                    tampered).getStatus());
  }

  @Test
  public void shouldFailWhenRecoveryByteMakesSignatureUnrecoverable() {
    byte[] tampered = signUnlock().encode();
    tampered[64] = 2;

    Assertions.assertEquals(
            VerificationStatus.FAIL,
            this.verifier.verify(this.encodedPredicate, this.sourceStateHash, this.transactionHash,
                    tampered).getStatus());
  }
}
