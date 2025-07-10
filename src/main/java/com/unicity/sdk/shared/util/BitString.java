package com.unicity.sdk.shared.util;

import com.unicity.sdk.shared.hash.DataHash;
import java.math.BigInteger;
import java.util.Arrays;

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
        // Add a leading byte 1 to ensure leading zeros are retained
        byte[] withLeadingOne = new byte[data.length + 1];
        withLeadingOne[0] = 0x01;
        System.arraycopy(data, 0, withLeadingOne, 1, data.length);
        this.value = new BigInteger(1, withLeadingOne);
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
        byte[] bytes = value.toByteArray();
        // Remove the leading 0x01 byte we added
        if (bytes.length > 1 && bytes[0] == 0x01) {
            return Arrays.copyOfRange(bytes, 1, bytes.length);
        }
        // Handle case where BigInteger added its own sign byte
        if (bytes.length > 2 && bytes[0] == 0x00 && bytes[1] == 0x01) {
            return Arrays.copyOfRange(bytes, 2, bytes.length);
        }
        return bytes;
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