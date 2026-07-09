package org.unicitylabs.sdk.smt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SparseMerkleTreePathUtilsTest {

  @Test
  public void calculatesCommonPrefixLength() {
    byte[] a = new byte[32];
    byte[] b = new byte[32];

    Assertions.assertEquals(256, SparseMerkleTreePathUtils.commonPrefixLength(a, b, 256));
    Assertions.assertEquals(10, SparseMerkleTreePathUtils.commonPrefixLength(a, b, 10));

    b[0] = (byte) 0x80;
    Assertions.assertEquals(0, SparseMerkleTreePathUtils.commonPrefixLength(a, b, 256));

    b[0] = (byte) 0x01;
    Assertions.assertEquals(7, SparseMerkleTreePathUtils.commonPrefixLength(a, b, 256));

    b[0] = (byte) 0x00;
    b[2] = (byte) 0x01;
    Assertions.assertEquals(23, SparseMerkleTreePathUtils.commonPrefixLength(a, b, 256));
    Assertions.assertEquals(10, SparseMerkleTreePathUtils.commonPrefixLength(a, b, 10));
  }

  @Test
  public void getBitAtDepthFollowsBigEndianConvention() {
    byte[] key = new byte[32];
    key[0] = (byte) 0b1000_0001;
    key[31] = (byte) 0x01;

    Assertions.assertEquals(1, SparseMerkleTreePathUtils.getBitAtDepth(key, 0));
    Assertions.assertEquals(0, SparseMerkleTreePathUtils.getBitAtDepth(key, 1));
    Assertions.assertEquals(1, SparseMerkleTreePathUtils.getBitAtDepth(key, 7));
    Assertions.assertEquals(0, SparseMerkleTreePathUtils.getBitAtDepth(key, 254));
    Assertions.assertEquals(1, SparseMerkleTreePathUtils.getBitAtDepth(key, 255));
  }

  @Test
  public void getBitAtDepthRejectsOutOfBounds() {
    byte[] key = new byte[32];
    Assertions.assertThrows(IllegalArgumentException.class,
            () -> SparseMerkleTreePathUtils.getBitAtDepth(key, -1));
    Assertions.assertThrows(IllegalArgumentException.class,
            () -> SparseMerkleTreePathUtils.getBitAtDepth(key, 256));
  }

  @Test
  public void regionFromKeyPacksPrefixIntoHighOrderBits() {
    byte[] key = new byte[32];

    key[0] = (byte) 0b1010_1111;
    byte[] region = SparseMerkleTreePathUtils.regionFromKey(key, 3);
    byte[] expected = new byte[32];
    expected[0] = (byte) 0b1010_0000;
    Assertions.assertArrayEquals(expected, region);

    key[0] = (byte) 0b1000_0001;
    key[1] = (byte) 0b1100_0000;
    byte[] spill = SparseMerkleTreePathUtils.regionFromKey(key, 9);
    byte[] spillExpected = new byte[32];
    spillExpected[0] = (byte) 0b1000_0001;
    spillExpected[1] = (byte) 0b1000_0000;
    Assertions.assertArrayEquals(spillExpected, spill);
  }
}
