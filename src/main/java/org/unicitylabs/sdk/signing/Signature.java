
package org.unicitylabs.sdk.signing;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.unicitylabs.sdk.util.HexConverter;
import java.util.Arrays;
import java.util.Objects;

@JsonSerialize(using = SignatureJson.Serializer.class)
@JsonDeserialize(using = SignatureJson.Deserializer.class)
public class Signature {

  private final byte[] bytes;
  private final int recovery;

  Signature(byte[] bytes, int recovery) {
    this.bytes = Arrays.copyOf(bytes, bytes.length);
    this.recovery = recovery;
  }

  public byte[] getBytes() {
    return Arrays.copyOf(this.bytes, this.bytes.length);
  }

  public int getRecovery() {
    return this.recovery;
  }

  /**
   * Encodes the signature with recovery byte appended.
   *
   * @return The encoded signature bytes
   */
  public byte[] encode() {
    byte[] signature = new byte[this.bytes.length + 1];
    System.arraycopy(this.bytes, 0, signature, 0, this.bytes.length);
    signature[this.bytes.length] = (byte) this.recovery;
    return signature;
  }

  /**
   * Decodes a byte array into a Signature object.
   *
   * @param input The byte array containing the signature (64 bytes + 1 recovery byte)
   * @return A Signature object
   */
  public static Signature decode(byte[] input) {
    if (input == null || input.length != 65) {
      throw new IllegalArgumentException("Invalid signature bytes. Expected 65 bytes.");
    }

    byte[] bytes = Arrays.copyOf(input, 64);
    int recovery = input[64] & 0xFF; // Ensure recovery is unsigned
    return new Signature(bytes, recovery);
  }

  @Override
  public boolean equals(Object o) {
      if (!(o instanceof Signature)) {
          return false;
      }
    Signature signature = (Signature) o;
    return this.recovery == signature.recovery && Objects.deepEquals(this.bytes, signature.bytes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(Arrays.hashCode(this.bytes), this.recovery);
  }

  @Override
  public String toString() {
    return String.format("Signature{bytes=%s, recovery=%s}", HexConverter.encode(this.bytes),
        this.recovery);
  }
}
