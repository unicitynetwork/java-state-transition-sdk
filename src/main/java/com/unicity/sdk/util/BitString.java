package com.unicity.sdk.util;

import com.unicity.sdk.hash.DataHash;
import java.math.BigInteger;

/**
 * Represents a bit string as a BigInteger.
 * This class is used to ensure that leading zero bits are retained when converting between byte arrays and BigInteger.
 */
public class BitString {
    private final BigInteger value;

    /**
     * Creates a BitString from a byte array.
     * @param data The input data to convert into a BitString.
     */
    public BitString(byte[] data) {
        // Create hex string with "01" prefix
        String hexString = "01" + HexConverter.encode(data);
        this.value = new BigInteger(hexString, 16);
    }

    /**
     * Creates a BitString from a DataHash imprint.
     * @param dataHash DataHash
     * @return A BitString instance
     */
    public static BitString fromDataHash(DataHash dataHash) {
        return new BitString(dataHash.getImprint());
    }

    /**
     * Converts BitString to BigInteger by adding a leading byte 1 to input byte array.
     * This is to ensure that the BigInteger will retain the leading zero bits.
     * @return The BigInteger representation of the bit string
     */
    public BigInteger toBigInteger() {
        return value;
    }

    /**
     * Converts bit string to byte array.
     * @return The byte array representation of the bit string
     */
    public byte[] toBytes() {
        // Convert to hex string, remove the "01" prefix we added, then convert back to bytes
        String hex = value.toString(16);
        if (hex.startsWith("1") && hex.length() > 2) {
            // Remove the leading "1" from "10000..."
            hex = hex.substring(1);
        }
        return HexConverter.decode(hex);
    }

    /**
     * Converts bit string to string.
     * @return The string representation of the bit string in binary format
     */
    @Override
    public String toString() {
        String binary = value.toString(2);
        // Remove the leading '1' bit we added
        if (binary.length() > 1 && binary.charAt(0) == '1') {
            return binary.substring(1);
        }
        return binary;
    }
}