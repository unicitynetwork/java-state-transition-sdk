package com.unicity.sdk.shared.hash;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataHasherTest {
    @Test
    public void testSha256() {
        DataHash hash = DataHasher.digest(HashAlgorithm.SHA256, "hello".getBytes(StandardCharsets.UTF_8));
        assertEquals(HashAlgorithm.SHA256, hash.getAlgorithm());
        assertArrayEquals(
                new byte[]{
                        (byte) 0x2c, (byte) 0xf2, (byte) 0x4d, (byte) 0xba, (byte) 0x5f, (byte) 0xb0, (byte) 0xa3, (byte) 0x0e,
                        (byte) 0x26, (byte) 0xe8, (byte) 0x3b, (byte) 0x2a, (byte) 0xc5, (byte) 0xb9, (byte) 0xe2, (byte) 0x9e,
                        (byte) 0x1b, (byte) 0x16, (byte) 0x1e, (byte) 0x5c, (byte) 0x1f, (byte) 0xa7, (byte) 0x42, (byte) 0x5e,
                        (byte) 0x73, (byte) 0x04, (byte) 0x33, (byte) 0x62, (byte) 0x93, (byte) 0x8b, (byte) 0x98, (byte) 0x24
                },
                hash.getData()
        );

        DataHash hash2 = DataHasher.digest(HashAlgorithm.SHA256, "world".getBytes(StandardCharsets.UTF_8));
        assertEquals(HashAlgorithm.SHA256, hash2.getAlgorithm());
        assertArrayEquals(
                new byte[]{
                        (byte) 0x48, (byte) 0x6e, (byte) 0xa4, (byte) 0x62, (byte) 0x24, (byte) 0xd1, (byte) 0xbb, (byte) 0x4f,
                        (byte) 0xb6, (byte) 0x80, (byte) 0xf3, (byte) 0x4f, (byte) 0x7c, (byte) 0x9a, (byte) 0xd9, (byte) 0x6a,
                        (byte) 0x8f, (byte) 0x24, (byte) 0xec, (byte) 0x88, (byte) 0xbe, (byte) 0x73, (byte) 0xea, (byte) 0x8e,
                        (byte) 0x5a, (byte) 0x6c, (byte) 0x65, (byte) 0x26, (byte) 0x0e, (byte) 0x9c, (byte) 0xb8, (byte) 0xa7
                },
                hash2.getData()
        );
    }
}