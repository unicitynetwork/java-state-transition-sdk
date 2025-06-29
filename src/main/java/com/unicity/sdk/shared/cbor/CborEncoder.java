
package com.unicity.sdk.shared.cbor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class CborEncoder {
    
    public static <T> byte[] encodeOptional(T data, Function<T, byte[]> encoder) {
        if (data == null) {
            return new byte[] { (byte) 0xf6 };
        }
        return encoder.apply(data);
    }
    
    public static byte[] encodeUnsignedInteger(long input) {
        if (input < 0) {
            throw new CborError("Only unsigned numbers are allowed.");
        }
        
        if (input < 24) {
            return new byte[] { (byte) (MajorType.UNSIGNED_INTEGER.getValue() | input) };
        }
        
        byte[] bytes = getUnsignedIntegerAsPaddedBytes(input);
        byte[] result = new byte[bytes.length + 1];
        result[0] = (byte) (MajorType.UNSIGNED_INTEGER.getValue() | getAdditionalInformationBits(bytes.length));
        System.arraycopy(bytes, 0, result, 1, bytes.length);
        return result;
    }
    
    public static byte[] encodeUnsignedInteger(BigInteger input) {
        if (input.signum() < 0) {
            throw new CborError("Only unsigned numbers are allowed.");
        }
        return encodeUnsignedInteger(input.longValue());
    }
    
    public static byte[] encodeByteString(byte[] input) {
        if (input.length < 24) {
            byte[] result = new byte[input.length + 1];
            result[0] = (byte) (MajorType.BYTE_STRING.getValue() | input.length);
            System.arraycopy(input, 0, result, 1, input.length);
            return result;
        }
        
        byte[] lengthBytes = getUnsignedIntegerAsPaddedBytes(input.length);
        byte[] result = new byte[1 + lengthBytes.length + input.length];
        result[0] = (byte) (MajorType.BYTE_STRING.getValue() | getAdditionalInformationBits(lengthBytes.length));
        System.arraycopy(lengthBytes, 0, result, 1, lengthBytes.length);
        System.arraycopy(input, 0, result, 1 + lengthBytes.length, input.length);
        return result;
    }
    
    public static byte[] encodeTextString(String input) {
        byte[] bytes = input.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (bytes.length < 24) {
            byte[] result = new byte[bytes.length + 1];
            result[0] = (byte) (MajorType.TEXT_STRING.getValue() | bytes.length);
            System.arraycopy(bytes, 0, result, 1, bytes.length);
            return result;
        }
        
        byte[] lengthBytes = getUnsignedIntegerAsPaddedBytes(bytes.length);
        byte[] result = new byte[1 + lengthBytes.length + bytes.length];
        result[0] = (byte) (MajorType.TEXT_STRING.getValue() | getAdditionalInformationBits(lengthBytes.length));
        System.arraycopy(lengthBytes, 0, result, 1, lengthBytes.length);
        System.arraycopy(bytes, 0, result, 1 + lengthBytes.length, bytes.length);
        return result;
    }
    
    public static byte[] encodeArray(List<byte[]> input) {
        int totalLength = input.stream().mapToInt(arr -> arr.length).sum();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        for (byte[] item : input) {
            try {
                baos.write(item);
            } catch (IOException e) {
                throw new CborError("Failed to encode array", e);
            }
        }
        byte[] data = baos.toByteArray();
        
        if (input.size() < 24) {
            byte[] result = new byte[1 + data.length];
            result[0] = (byte) (MajorType.ARRAY.getValue() | input.size());
            System.arraycopy(data, 0, result, 1, data.length);
            return result;
        }
        
        byte[] lengthBytes = getUnsignedIntegerAsPaddedBytes(input.size());
        byte[] result = new byte[1 + lengthBytes.length + data.length];
        result[0] = (byte) (MajorType.ARRAY.getValue() | getAdditionalInformationBits(lengthBytes.length));
        System.arraycopy(lengthBytes, 0, result, 1, lengthBytes.length);
        System.arraycopy(data, 0, result, 1 + lengthBytes.length, data.length);
        return result;
    }
    
    public static byte[] encodeArray(byte[]... input) {
        return encodeArray(Arrays.asList(input));
    }
    
    public static byte[] encodeBoolean(boolean data) {
        if (data) {
            return new byte[] { (byte) 0xf5 };
        }
        return new byte[] { (byte) 0xf4 };
    }
    
    public static byte[] encodeNull() {
        return new byte[] { (byte) 0xf6 };
    }
    
    private static int getAdditionalInformationBits(int length) {
        return 24 + (int) Math.ceil(Math.log(length) / Math.log(2));
    }
    
    private static byte[] getUnsignedIntegerAsPaddedBytes(long input) {
        if (input < 0) {
            throw new CborError("Only unsigned numbers are allowed.");
        }
        
        if (input == 0) {
            return new byte[] { 0 };
        }
        
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        long t = input;
        while (t > 0) {
            bytes.write((byte) (t & 0xFF));
            t = t >>> 8;
        }
        
        byte[] rawBytes = bytes.toByteArray();
        
        if (rawBytes.length > 8) {
            throw new CborError("Number is not unsigned long.");
        }
        
        // Reverse the bytes
        for (int i = 0; i < rawBytes.length / 2; i++) {
            byte temp = rawBytes[i];
            rawBytes[i] = rawBytes[rawBytes.length - 1 - i];
            rawBytes[rawBytes.length - 1 - i] = temp;
        }
        
        // Pad to power of 2 length
        int paddedLength = (int) Math.pow(2, Math.ceil(Math.log(rawBytes.length) / Math.log(2)));
        if (paddedLength == rawBytes.length) {
            return rawBytes;
        }
        
        byte[] paddedBytes = new byte[paddedLength];
        System.arraycopy(rawBytes, 0, paddedBytes, paddedLength - rawBytes.length, rawBytes.length);
        return paddedBytes;
    }

    /**
     * Encode the start of a CBOR map
     */
    public static byte[] encodeMapStart(int length) {
        if (length < 24) {
            return new byte[] { (byte) (MajorType.MAP.getValue() | length) };
        } else if (length <= 0xFF) {
            return new byte[] { (byte) (MajorType.MAP.getValue() | 24), (byte) length };
        } else if (length <= 0xFFFF) {
            return new byte[] {
                (byte) (MajorType.MAP.getValue() | 25),
                (byte) (length >> 8),
                (byte) length
            };
        } else {
            return new byte[] {
                (byte) (MajorType.MAP.getValue() | 26),
                (byte) (length >> 24),
                (byte) (length >> 16),
                (byte) (length >> 8),
                (byte) length
            };
        }
    }

    /**
     * Encode an unsigned integer (int)
     */
    public static byte[] encodeUnsignedInteger(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value must be non-negative");
        }
        
        if (value < 24) {
            return new byte[] { (byte) (MajorType.UNSIGNED_INTEGER.getValue() | value) };
        } else if (value <= 0xFF) {
            return new byte[] { (byte) (MajorType.UNSIGNED_INTEGER.getValue() | 24), (byte) value };
        } else if (value <= 0xFFFF) {
            return new byte[] {
                (byte) (MajorType.UNSIGNED_INTEGER.getValue() | 25),
                (byte) (value >> 8),
                (byte) value
            };
        } else {
            return new byte[] {
                (byte) (MajorType.UNSIGNED_INTEGER.getValue() | 26),
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
            };
        }
    }

}
