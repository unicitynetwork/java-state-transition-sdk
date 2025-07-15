package com.unicity.sdk.shared.cbor;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class CborEncoderTest {
    
    @Test
    public void testEncodeByteString() {
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        byte[] encoded = CborEncoder.encodeByteString(data);
        
        // Expected: 0x45 (byte string major type | length 5) + "hello"
        byte[] expected = new byte[] { 0x45, 0x68, 0x65, 0x6c, 0x6c, 0x6f };
        assertArrayEquals(expected, encoded);
    }
    
    @Test
    public void testEncodeTextString() {
        byte[] encoded = CborEncoder.encodeTextString("hello");
        
        // Expected: 0x65 (text string major type | length 5) + "hello" in UTF-8
        byte[] expected = new byte[] { 0x65, 0x68, 0x65, 0x6c, 0x6c, 0x6f };
        assertArrayEquals(expected, encoded);
    }
    
    @Test
    public void testEncodeArray() {
        byte[] item1 = CborEncoder.encodeTextString("hello");
        byte[] item2 = CborEncoder.encodeTextString("world");
        byte[] encoded = CborEncoder.encodeArray(item1, item2);
        
        // Expected: 0x82 (array major type | length 2) + encoded items
        assertEquals(0x82, encoded[0] & 0xFF);
        assertEquals(1 + item1.length + item2.length, encoded.length);
    }
    
    @Test
    public void testEncodeNull() {
        byte[] encoded = CborEncoder.encodeNull();
        assertArrayEquals(new byte[] { (byte) 0xf6 }, encoded);
    }
    
    @Test
    public void testEncodeOptional() {
        // Test with null value
        byte[] nullEncoded = CborEncoder.encodeOptional(null, CborEncoder::encodeTextString);
        assertArrayEquals(new byte[] { (byte) 0xf6 }, nullEncoded);
        
        // Test with non-null value
        byte[] valueEncoded = CborEncoder.encodeOptional("test", CborEncoder::encodeTextString);
        assertArrayEquals(CborEncoder.encodeTextString("test"), valueEncoded);
    }
    
    @Test
    public void testEncodeBoolean() {
        byte[] trueEncoded = CborEncoder.encodeBoolean(true);
        assertArrayEquals(new byte[] { (byte) 0xf5 }, trueEncoded);
        
        byte[] falseEncoded = CborEncoder.encodeBoolean(false);
        assertArrayEquals(new byte[] { (byte) 0xf4 }, falseEncoded);
    }
    
    @Test
    public void testEncodeLargeByteString() {
        // Test with a byte string longer than 23 bytes
        byte[] data = new byte[100];
        Arrays.fill(data, (byte) 0xAB);
        
        byte[] encoded = CborEncoder.encodeByteString(data);
        
        // Should have: major type byte + length bytes + data
        assertEquals(0x58, encoded[0] & 0xFF); // 0x40 (byte string) | 0x18 (1-byte length follows)
        assertEquals(100, encoded[1] & 0xFF); // Length byte
        assertEquals(102, encoded.length); // 1 + 1 + 100
    }
}