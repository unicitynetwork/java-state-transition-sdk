package com.unicity.sdk.shared.util;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Utility class for converting BigInteger to/from byte arrays.
 * Matches the TypeScript BigintConverter behavior.
 */
public class BigIntegerConverter {
    
    /**
     * Encode a BigInteger as a byte array in little-endian format.
     * This matches the TypeScript implementation which uses little-endian encoding.
     * 
     * @param value The BigInteger to encode
     * @return Byte array representation
     */
    public static byte[] encode(BigInteger value) {
        if (value == null) {
            throw new IllegalArgumentException("Value cannot be null");
        }
        
        if (value.equals(BigInteger.ZERO)) {
            return new byte[0];
        }
        
        // Get the byte array in big-endian format
        byte[] bigEndianBytes = value.toByteArray();
        
        // Remove leading zero byte if present (Java adds this for positive numbers)
        int offset = 0;
        if (bigEndianBytes.length > 1 && bigEndianBytes[0] == 0) {
            offset = 1;
        }
        
        // Convert to little-endian
        int length = bigEndianBytes.length - offset;
        byte[] littleEndianBytes = new byte[length];
        for (int i = 0; i < length; i++) {
            littleEndianBytes[i] = bigEndianBytes[bigEndianBytes.length - 1 - i];
        }
        
        return littleEndianBytes;
    }
    
    /**
     * Decode a byte array in little-endian format to a BigInteger.
     * 
     * @param bytes The byte array to decode
     * @return BigInteger representation
     */
    public static BigInteger decode(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("Bytes cannot be null");
        }
        
        if (bytes.length == 0) {
            return BigInteger.ZERO;
        }
        
        // Convert from little-endian to big-endian
        byte[] bigEndianBytes = new byte[bytes.length + 1]; // Extra byte for sign
        bigEndianBytes[0] = 0; // Positive sign
        
        for (int i = 0; i < bytes.length; i++) {
            bigEndianBytes[i + 1] = bytes[bytes.length - 1 - i];
        }
        
        return new BigInteger(bigEndianBytes);
    }
}