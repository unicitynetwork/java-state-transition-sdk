package org.unicitylabs.sdk.serializer.cbor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer.CborMap;
import org.unicitylabs.sdk.serializer.cbor.CborSerializer.CborMap.Entry;

public class CborDeserializer {

  private final static byte MAJOR_TYPE_MASK = (byte) 0b11100000;
  private final static byte ADDITIONAL_INFORMATION_MASK = (byte) 0b00011111;

  public static <T> T readOptional(byte[] data, Function<byte[], T> reader) {
    if (Byte.compareUnsigned(new CborReader(data).readByte(), (byte) 0xf6) == 0) {
      return null;
    }

    return reader.apply(data);
  }

  public static CborNumber readUnsignedInteger(byte[] data) {
    CborReader reader = new CborReader(data);
    return new CborNumber(reader.readLength(CborMajorType.UNSIGNED_INTEGER));
  }


  public static byte[] readByteString(byte[] data) {
    CborReader reader = new CborReader(data);
    return reader.read((int) reader.readLength(CborMajorType.BYTE_STRING));
  }

  public static String readTextString(byte[] data) {
    CborReader reader = new CborReader(data);
    return new String(
        reader.read((int) reader.readLength(CborMajorType.TEXT_STRING)));
  }


  public static List<byte[]> readArray(byte[] data) {
    CborReader reader = new CborReader(data);
    long length = reader.readLength(CborMajorType.ARRAY);

    ArrayList<byte[]> result = new ArrayList<>();
    for (int i = 0; i < length; i++) {
      result.add(reader.readRawCBOR());
    }

    return result;
  }

  public static Set<CborMap.Entry> readMap(byte[] data) {
    CborReader reader = new CborReader(data);
    long length = (int) reader.readLength(CborMajorType.MAP);

    Set<Entry> result = new LinkedHashSet<>();
    for (int i = 0; i < length; i++) {
      byte[] key = reader.readRawCBOR();
      byte[] value = reader.readRawCBOR();
      result.add(new CborMap.Entry(key, value));
    }

    return result;
  }

  public static CborTag readTag(byte[] data) {
    CborReader reader = new CborReader(data);
    long tag = (int) reader.readLength(CborMajorType.TAG);
    return new CborTag(tag, reader.readRawCBOR());
  }

  public static boolean readBoolean(byte[] data) {
    byte byteValue = new CborReader(data).readByte();
    if (byteValue == (byte) 0xf5) {
      return true;
    }
    if (byteValue == (byte) 0xf4) {
      return false;
    }
    throw new CborSerializationException("Type mismatch, expected boolean.");
  }

  private static class CborReader {
    final byte[] data;
    int position = 0;

    CborReader(byte[] data) {
      Objects.requireNonNull(data, "Input byte array cannot be null.");

      this.data = data;
    }

    public byte readByte() {
      if (this.position >= this.data.length) {
        throw new CborSerializationException("Premature end of data.");
      }

      return this.data[this.position++];
    }

    public byte[] read(int length) {
      try {
        if ((this.position + length) > this.data.length) {
          throw new CborSerializationException("Premature end of data.");
        }

        return Arrays.copyOfRange(this.data, this.position, this.position + length);
      } finally {
        this.position += length;
      }
    }

    public long readLength(CborMajorType majorType) {
      byte initialByte = this.readByte();

      if (CborMajorType.fromType(initialByte & CborDeserializer.MAJOR_TYPE_MASK) != majorType) {
        throw new CborSerializationException("Major type mismatch.");
      }

      byte additionalInformation = (byte) (initialByte
          & CborDeserializer.ADDITIONAL_INFORMATION_MASK);
      if (Byte.compareUnsigned(additionalInformation, (byte) 24) < 0) {
        return additionalInformation;
      }

      switch (majorType) {
        case ARRAY:
        case BYTE_STRING:
        case TEXT_STRING:
          if (Byte.compareUnsigned(additionalInformation, (byte) 31) == 0) {
            throw new CborSerializationException("Indefinite length array not supported.");
          }
      }

      if (Byte.compareUnsigned(additionalInformation, (byte) 27) > 0) {
        throw new CborSerializationException("Encoded item is not well-formed.");
      }

      long t = 0;
      int length = (int) Math.pow(2, additionalInformation - 24);
      for (int i = 0; i < length; ++i) {
        t = (t << 8) | this.readByte() & 0xFF;
      }

      return t;
    }

    public byte[] readRawCBOR() {
      if (this.position >= this.data.length) {
        throw new CborSerializationException("Premature end of data.");
      }

      CborMajorType majorType = CborMajorType.fromType(this.data[this.position] & CborDeserializer.MAJOR_TYPE_MASK);
      int position = this.position;
      int length = (int) this.readLength(majorType);
      switch (majorType) {
        case BYTE_STRING:
        case TEXT_STRING:
          this.read(length);
          break;
        case ARRAY:
          for (int i = 0; i < length; i++) {
            this.readRawCBOR();
          }
          break;
        case MAP:
          for (int i = 0; i < length; i++) {
            this.readRawCBOR();
            this.readRawCBOR();
          }
          break;
        case TAG:
          this.readRawCBOR();
          break;
      }

      return Arrays.copyOfRange(this.data, position, this.position);
    }
  }

  public static class CborTag {
    private final long tag;
    private final byte[] data;

    private CborTag(long tag, byte[] data) {
      this.tag = tag;
      this.data = data;
    }

    public long getTag() {
      return this.tag;
    }

    public byte[] getData() {
      return Arrays.copyOf(this.data, this.data.length);
    }
  }

  public static class CborNumber {
    private final long value;

    public CborNumber(long value) {
      this.value = value;
    }

    public long asLong() {
      return this.value;
    }

    public int asInt() {
      if (Long.compareUnsigned(this.value, 0xFFFFFFFFL) > 0) {
        throw new ArithmeticException("Value too large");
      }
      return (int) this.value;
    }

    public byte asByte() {
      if (Long.compareUnsigned(this.value, 0xFFL) > 0) {
        throw new ArithmeticException("Value too large");
      }

      return (byte) this.value;
    }

    public short asShort() {
      if (Long.compareUnsigned(this.value, 0xFFFFL) > 0) {
        throw new ArithmeticException("Value too large");
      }

      return (short) this.value;
    }
  }
}
