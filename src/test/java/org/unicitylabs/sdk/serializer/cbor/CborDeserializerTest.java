package org.unicitylabs.sdk.serializer.cbor;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.unicitylabs.sdk.serializer.cbor.CborDeserializer.CborTag;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer.CborMap;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer.CborMap.Entry;
import org.unicitylabs.sdk.util.HexConverter;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CborDeserializerTest {

  @Test
  void testReadUnsignedInteger() {
    Assertions.assertEquals(
            5,
            CborDeserializer.decodeUnsignedInteger(HexConverter.decode("05")).asLong()
    );

    Assertions.assertEquals(
            100,
            CborDeserializer.decodeUnsignedInteger(HexConverter.decode("1864")).asLong()
    );

    Assertions.assertEquals(
            10000,
            CborDeserializer.decodeUnsignedInteger(HexConverter.decode("192710")).asLong()
    );

    Assertions.assertEquals(
            66000,
            CborDeserializer.decodeUnsignedInteger(HexConverter.decode("1a000101d0")).asLong()
    );

    Assertions.assertEquals(
            8147483647L,
            CborDeserializer.decodeUnsignedInteger(HexConverter.decode("1b00000001e5a0bbff")).asLong()
    );

    Assertions.assertEquals(
            -5,
            CborDeserializer.decodeUnsignedInteger(HexConverter.decode("1bfffffffffffffffb")).asLong()
    );
  }

  @Test
  void testReadByteString() {
    Assertions.assertArrayEquals(
            new byte[5],
            CborDeserializer.decodeByteString(HexConverter.decode("450000000000"))
    );

    Assertions.assertArrayEquals(
            new byte[25],
            CborDeserializer.decodeByteString(
                    HexConverter.decode("581900000000000000000000000000000000000000000000000000"))
    );
  }

  @Test
  void testReadTextString() {
    Assertions.assertEquals(
            "Hello, world!",
            CborDeserializer.decodeTextString(HexConverter.decode("6d48656c6c6f2c20776f726c6421"))
    );

    Assertions.assertEquals(
            new String(new byte[25]),
            CborDeserializer.decodeTextString(
                    HexConverter.decode("781900000000000000000000000000000000000000000000000000"))
    );
  }

  @Test
  void testReadArray() {
    List<byte[]> data = CborDeserializer.decodeArray(
            HexConverter.decode(
                    "98196d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c64216d48656c6c6f2c20776f726c6421")
    );

    for (byte[] item : data) {
      Assertions.assertEquals("Hello, world!", CborDeserializer.decodeTextString(item));
    }
  }

  @Test
  void testReadMap() {
    Set<CborMap.Entry> data = CborDeserializer.decodeMap(
            HexConverter.decode(
                    "a4430000006d48656c6c6f2c20776f726c6421430000016d48656c6c6f2c20776f726c64216454657374f66d48656c6c6f2c20776f726c6421581900000000000000000000000000000000000000000000000000")
    );

    Iterator<Entry> iterator = data.iterator();
    Entry entry = iterator.next();
    Assertions.assertArrayEquals(
            CborSerializer.encodeByteString(HexConverter.decode("000000")),
            entry.getKey()
    );
    Assertions.assertArrayEquals(
            CborSerializer.encodeTextString("Hello, world!"),
            entry.getValue()
    );

    entry = iterator.next();
    Assertions.assertArrayEquals(
            CborSerializer.encodeByteString(HexConverter.decode("000001")),
            entry.getKey()
    );
    Assertions.assertArrayEquals(
            CborSerializer.encodeTextString("Hello, world!"),
            entry.getValue()
    );

    entry = iterator.next();
    Assertions.assertArrayEquals(
            CborSerializer.encodeTextString("Test"),
            entry.getKey()
    );
    Assertions.assertArrayEquals(
            CborSerializer.encodeNull(),
            entry.getValue()
    );

    entry = iterator.next();
    Assertions.assertArrayEquals(
            CborSerializer.encodeTextString("Hello, world!"),
            entry.getKey()
    );
    Assertions.assertArrayEquals(
            CborSerializer.encodeByteString(new byte[25]),
            entry.getValue()
    );
  }

  @Test
  void testReadBoolean() {
    Assertions.assertTrue(CborDeserializer.decodeBoolean(HexConverter.decode("f5")));

    Assertions.assertFalse(CborDeserializer.decodeBoolean(HexConverter.decode("f4")));
  }

  @Test
  void testReadOptional() {
    Assertions.assertNull(
            CborDeserializer.decodeNullable(
                    HexConverter.decode("f6"),
                    CborDeserializer::decodeUnsignedInteger
            )
    );
  }

  @Test
  void testEncodeTag() {
    CborTag tag = CborDeserializer.decodeTag(
            HexConverter.decode("d4781a746167206e756d62657220736d616c6c6572207468616e203234")
    );
    Assertions.assertEquals(
            20,
            tag.getTag()
    );

    Assertions.assertArrayEquals(
            CborSerializer.encodeTextString("tag number smaller than 24"),
            tag.getData()
    );
  }

  @Test
  void testReadRawCborStopsAtItemBoundary() {
    // [1, [2], 3] - nested item must be read completely and end exactly at its boundary.
    List<byte[]> data = CborDeserializer.decodeArray(HexConverter.decode("8301810203"));

    Assertions.assertEquals(3, data.size());
    Assertions.assertArrayEquals(HexConverter.decode("01"), data.get(0));
    Assertions.assertArrayEquals(HexConverter.decode("8102"), data.get(1));
    Assertions.assertArrayEquals(HexConverter.decode("03"), data.get(2));
  }

  @Test
  void testReadDeeplyNestedCborWithoutStackOverflow() {
    int depth = 100_000;
    byte[] bytes = new byte[depth + 1];
    java.util.Arrays.fill(bytes, 0, depth, (byte) 0x81);
    bytes[depth] = 0x01;

    Assertions.assertArrayEquals(bytes, CborDeserializer.decodeNullable(bytes, data -> data));
  }

  @Test
  void testByteStringLengthOverflowIsRejected() {
    // Byte string claiming length 2^32 + 5 followed by 5 bytes: a narrowing (int) cast would
    // truncate the length to 5 and parse successfully instead of failing.
    Assertions.assertThrows(
            CborSerializationException.class,
            () -> CborDeserializer.decodeByteString(
                    HexConverter.decode("5b00000001000000050000000000"))
    );

    Assertions.assertThrows(
            CborSerializationException.class,
            () -> CborDeserializer.decodeTextString(
                    HexConverter.decode("7b00000001000000050000000000"))
    );

    // Byte string claiming Long.MAX_VALUE bytes.
    Assertions.assertThrows(
            CborSerializationException.class,
            () -> CborDeserializer.decodeByteString(HexConverter.decode("5b7fffffffffffffff"))
    );

    // Byte string claiming 2^64 - 1 bytes (-1 as a signed long): a signed comparison would
    // accept it and the narrowing cast would go negative.
    Assertions.assertThrows(
            CborSerializationException.class,
            () -> CborDeserializer.decodeByteString(HexConverter.decode("5bffffffffffffffff"))
    );
  }

  @Test
  void testOversizedCollectionLengthIsRejected() {
    // Array claiming 2^32 elements with no data.
    Assertions.assertThrows(
            CborSerializationException.class,
            () -> CborDeserializer.decodeArray(HexConverter.decode("9b0000000100000000"))
    );

    // Array claiming 2^64 - 1 elements: must fail cleanly, not wrap a counter.
    Assertions.assertThrows(
            CborSerializationException.class,
            () -> CborDeserializer.decodeArray(HexConverter.decode("9bffffffffffffffff"))
    );

    // Map claiming 2^32 entries with no data.
    Assertions.assertThrows(
            CborSerializationException.class,
            () -> CborDeserializer.decodeMap(HexConverter.decode("bb0000000100000000"))
    );

    // Nested inside an otherwise valid item: [oversized array].
    Assertions.assertThrows(
            CborSerializationException.class,
            () -> CborDeserializer.decodeNullable(
                    HexConverter.decode("819bffffffffffffffff"), data -> data)
    );
  }

  @Test
  void testLargeTagIsNotTruncated() {
    // Tag 2^33 (does not fit in int): narrowing would silently decode it as 0.
    CborTag tag = CborDeserializer.decodeTag(HexConverter.decode("db000000020000000001"));
    Assertions.assertEquals(8589934592L, tag.getTag());
  }

  @Test
  void testNumberNarrowingIsChecked() {
    Assertions.assertEquals(
            Integer.MAX_VALUE,
            CborDeserializer.decodeUnsignedInteger(HexConverter.decode("1a7fffffff")).asInt()
    );
    // 2^31 does not fit in a signed int; unchecked narrowing would return Integer.MIN_VALUE.
    Assertions.assertThrows(
            CborSerializationException.class,
            () -> CborDeserializer.decodeUnsignedInteger(HexConverter.decode("1a80000000")).asInt()
    );

    Assertions.assertEquals(
            Short.MAX_VALUE,
            CborDeserializer.decodeUnsignedInteger(HexConverter.decode("197fff")).asShort()
    );
    Assertions.assertThrows(
            CborSerializationException.class,
            () -> CborDeserializer.decodeUnsignedInteger(HexConverter.decode("198000")).asShort()
    );

    Assertions.assertEquals(
            Byte.MAX_VALUE,
            CborDeserializer.decodeUnsignedInteger(HexConverter.decode("187f")).asByte()
    );
    Assertions.assertThrows(
            CborSerializationException.class,
            () -> CborDeserializer.decodeUnsignedInteger(HexConverter.decode("1880")).asByte()
    );
  }
}
