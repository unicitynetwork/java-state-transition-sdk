package com.unicity.sdk.shared.util;

/**
 * Utility class for converting between byte arrays and hexadecimal strings.
 */
public class HexConverter {
    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    /**
     * Convert byte array to hex
     * @param data byte array
     * @return hex string
     */
    public static String encode(byte[] data) {
        char[] hexChars = new char[data.length * 2];
        for (int j = 0; j < data.length; j++) {
            int v = data[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * Convert hex string to bytes
     * @param value hex string
     * @return byte array
     */
    public static byte[] decode(String value) {
        int len = value.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(value.charAt(i), 16) << 4)
                                 + Character.digit(value.charAt(i+1), 16));
        }
        return data;
    }
}