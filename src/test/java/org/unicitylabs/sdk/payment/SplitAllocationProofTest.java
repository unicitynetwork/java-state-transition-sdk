package org.unicitylabs.sdk.payment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.crypto.hash.HashAlgorithm;
import org.unicitylabs.sdk.serializer.cbor.CborSerializationException;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer;

import java.math.BigInteger;

public class SplitAllocationProofTest {

  private static byte[] singleSiblingProof(int hashLength) {
    byte[] entry = CborSerializer.encodeArray(
            CborSerializer.encodeUnsignedInteger(0),
            CborSerializer.encodeByteString(new byte[hashLength]),
            CborSerializer.encodeBigInteger(BigInteger.ONE));
    return CborSerializer.encodeArray(entry);
  }

  @Test
  void fromCborRejectsNon32ByteSiblingHash() {
    Assertions.assertThrows(CborSerializationException.class,
            () -> SplitAllocationProof.fromCbor(singleSiblingProof(31)));
    Assertions.assertThrows(CborSerializationException.class,
            () -> SplitAllocationProof.fromCbor(singleSiblingProof(33)));
  }

  @Test
  void fromCborAcceptsSha256SiblingHash() {
    Assertions.assertDoesNotThrow(
            () -> SplitAllocationProof.fromCbor(singleSiblingProof(HashAlgorithm.SHA256.getLength())));
  }
}
