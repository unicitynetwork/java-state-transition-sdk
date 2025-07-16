
package com.unicity.sdk.signing;

import java.util.Arrays;
import java.util.Objects;

public class Signature {
    private final byte[] bytes;
    private final int recovery;

    public Signature(byte[] bytes, int recovery) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
        this.recovery = recovery;
    }

    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }
    
    public int getRecovery() {
        return recovery;
    }
    
    /**
     * Encodes the signature with recovery byte appended.
     * @return The encoded signature bytes
     */
    public byte[] encode() {
        byte[] signature = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, signature, 0, bytes.length);
        signature[bytes.length] = (byte) recovery;
        return signature;
    }

    /**
     * Decodes a byte array into a Signature object.
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
        if (!(o instanceof Signature)) return false;
        Signature signature = (Signature) o;
        return recovery == signature.recovery && Objects.deepEquals(bytes, signature.bytes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(bytes), recovery);
    }
}
