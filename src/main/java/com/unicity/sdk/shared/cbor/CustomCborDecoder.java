package com.unicity.sdk.shared.cbor;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom CBOR decoder that matches the TypeScript implementation.
 * This decoder provides a simple DecodeResult structure to handle CBOR decoding.
 */
public class CustomCborDecoder {
    
    public static class DecodeResult {
        public final Object value;
        public final int nextOffset;
        
        public DecodeResult(Object value, int nextOffset) {
            this.value = value;
            this.nextOffset = nextOffset;
        }
    }
    
    /**
     * Decode CBOR data starting at the given offset.
     * @param data The CBOR-encoded data
     * @param offset The offset to start decoding from
     * @return DecodeResult containing the decoded value and next offset
     */
    public static DecodeResult decode(byte[] data, int offset) {
        if (offset >= data.length) {
            throw new RuntimeException("Offset out of bounds");
        }
        
        byte majorType = (byte) ((data[offset] & 0xFF) >> 5);
        int additionalInfo = data[offset] & 0x1F;
        
        switch (majorType) {
            case 0: // Unsigned integer
                return decodeUnsignedInteger(data, offset);
            case 1: // Negative integer
                return decodeNegativeInteger(data, offset);
            case 2: // Byte string
                return decodeByteString(data, offset);
            case 3: // Text string
                return decodeTextString(data, offset);
            case 4: // Array
                return decodeArray(data, offset);
            case 5: // Map
                return decodeMap(data, offset);
            case 6: // Tagged item
                return decodeTaggedItem(data, offset);
            case 7: // Floating point, simple values, break
                return decodeSimpleValue(data, offset);
            default:
                throw new RuntimeException("Unknown major type: " + majorType);
        }
    }
    
    private static DecodeResult decodeUnsignedInteger(byte[] data, int offset) {
        int additionalInfo = data[offset] & 0x1F;
        if (additionalInfo < 24) {
            return new DecodeResult((long) additionalInfo, offset + 1);
        } else if (additionalInfo == 24) {
            return new DecodeResult((long) (data[offset + 1] & 0xFF), offset + 2);
        } else if (additionalInfo == 25) {
            ByteBuffer buffer = ByteBuffer.wrap(data, offset + 1, 2);
            return new DecodeResult((long) buffer.getShort(), offset + 3);
        } else if (additionalInfo == 26) {
            ByteBuffer buffer = ByteBuffer.wrap(data, offset + 1, 4);
            return new DecodeResult((long) buffer.getInt(), offset + 5);
        } else if (additionalInfo == 27) {
            ByteBuffer buffer = ByteBuffer.wrap(data, offset + 1, 8);
            return new DecodeResult(buffer.getLong(), offset + 9);
        } else {
            throw new RuntimeException("Invalid additional info for unsigned integer: " + additionalInfo);
        }
    }
    
    private static DecodeResult decodeNegativeInteger(byte[] data, int offset) {
        DecodeResult unsigned = decodeUnsignedInteger(data, offset);
        long value = -1L - (Long) unsigned.value;
        return new DecodeResult(value, unsigned.nextOffset);
    }
    
    private static DecodeResult decodeByteString(byte[] data, int offset) {
        DecodeResult lengthResult = decodeLength(data, offset);
        int length = ((Long) lengthResult.value).intValue();
        int dataOffset = lengthResult.nextOffset;
        
        byte[] bytes = new byte[length];
        System.arraycopy(data, dataOffset, bytes, 0, length);
        
        return new DecodeResult(bytes, dataOffset + length);
    }
    
    private static DecodeResult decodeTextString(byte[] data, int offset) {
        DecodeResult lengthResult = decodeLength(data, offset);
        int length = ((Long) lengthResult.value).intValue();
        int dataOffset = lengthResult.nextOffset;
        
        String text = new String(data, dataOffset, length, StandardCharsets.UTF_8);
        
        return new DecodeResult(text, dataOffset + length);
    }
    
    private static DecodeResult decodeArray(byte[] data, int offset) {
        DecodeResult lengthResult = decodeLength(data, offset);
        int length = ((Long) lengthResult.value).intValue();
        int currentOffset = lengthResult.nextOffset;
        
        List<Object> array = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            DecodeResult itemResult = decode(data, currentOffset);
            array.add(itemResult.value);
            currentOffset = itemResult.nextOffset;
        }
        
        return new DecodeResult(array, currentOffset);
    }
    
    private static DecodeResult decodeMap(byte[] data, int offset) {
        DecodeResult lengthResult = decodeLength(data, offset);
        int length = ((Long) lengthResult.value).intValue();
        int currentOffset = lengthResult.nextOffset;
        
        Map<Object, Object> map = new HashMap<>();
        for (int i = 0; i < length; i++) {
            DecodeResult keyResult = decode(data, currentOffset);
            currentOffset = keyResult.nextOffset;
            
            DecodeResult valueResult = decode(data, currentOffset);
            currentOffset = valueResult.nextOffset;
            
            map.put(keyResult.value, valueResult.value);
        }
        
        return new DecodeResult(map, currentOffset);
    }
    
    private static DecodeResult decodeTaggedItem(byte[] data, int offset) {
        // For now, just skip the tag and decode the next item
        DecodeResult tagResult = decodeUnsignedInteger(data, offset);
        return decode(data, tagResult.nextOffset);
    }
    
    private static DecodeResult decodeSimpleValue(byte[] data, int offset) {
        int additionalInfo = data[offset] & 0x1F;
        
        if (additionalInfo == 20) { // false
            return new DecodeResult(false, offset + 1);
        } else if (additionalInfo == 21) { // true
            return new DecodeResult(true, offset + 1);
        } else if (additionalInfo == 22) { // null
            return new DecodeResult(null, offset + 1);
        } else if (additionalInfo == 23) { // undefined
            return new DecodeResult(null, offset + 1); // Map undefined to null
        } else {
            throw new RuntimeException("Unsupported simple value: " + additionalInfo);
        }
    }
    
    private static DecodeResult decodeLength(byte[] data, int offset) {
        int additionalInfo = data[offset] & 0x1F;
        
        if (additionalInfo < 24) {
            return new DecodeResult((long) additionalInfo, offset + 1);
        } else if (additionalInfo == 24) {
            return new DecodeResult((long) (data[offset + 1] & 0xFF), offset + 2);
        } else if (additionalInfo == 25) {
            ByteBuffer buffer = ByteBuffer.wrap(data, offset + 1, 2);
            return new DecodeResult((long) (buffer.getShort() & 0xFFFF), offset + 3);
        } else if (additionalInfo == 26) {
            ByteBuffer buffer = ByteBuffer.wrap(data, offset + 1, 4);
            return new DecodeResult(buffer.getInt() & 0xFFFFFFFFL, offset + 5);
        } else if (additionalInfo == 27) {
            ByteBuffer buffer = ByteBuffer.wrap(data, offset + 1, 8);
            return new DecodeResult(buffer.getLong(), offset + 9);
        } else if (additionalInfo == 31) { // Indefinite length
            return new DecodeResult(-1L, offset + 1);
        } else {
            throw new RuntimeException("Invalid length encoding: " + additionalInfo);
        }
    }
}