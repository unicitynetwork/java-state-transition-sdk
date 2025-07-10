
package com.unicity.sdk.shared.hash;

import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORGenerator;
import com.unicity.sdk.ISerializable;
import com.unicity.sdk.util.HexConverter;
import com.unicity.sdk.shared.cbor.CborDecoder;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.cbor.CustomCborDecoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class DataHash implements ISerializable {
    private final byte[] hash;
    private final HashAlgorithm algorithm;

    public DataHash(byte[] hash, HashAlgorithm algorithm) {
        this.hash = Arrays.copyOf(hash, hash.length);
        this.algorithm = algorithm;
    }

    /**
     * Creates a DataHash from an imprint (algorithm prefix + hash bytes).
     * The imprint format is: [algorithm_byte_1][algorithm_byte_2][hash_bytes...]
     * 
     * @param imprint The imprint bytes containing algorithm identifier and hash
     * @return A new DataHash instance
     */
    public static DataHash fromImprint(byte[] imprint) {
        if (imprint.length < 3) {
            throw new IllegalArgumentException("Imprint too short");
        }
        
        // Extract algorithm from first two bytes
        int algorithmValue = ((imprint[0] & 0xFF) << 8) | (imprint[1] & 0xFF);
        HashAlgorithm algorithm = HashAlgorithm.values()[algorithmValue];
        
        // Extract hash data
        byte[] hashData = Arrays.copyOfRange(imprint, 2, imprint.length);
        
        return new DataHash(hashData, algorithm);
    }
    
    /**
     * Create DataHash from JSON representation.
     * Expects a hex string in imprint format (algorithm prefix + hash).
     */
    public static DataHash fromJSON(String imprintHex) {
        return fromImprint(HexConverter.decode(imprintHex));
    }

    public byte[] getHash() {
        return Arrays.copyOf(hash, hash.length);
    }

    public HashAlgorithm getAlgorithm() {
        return algorithm;
    }

    @Override
    public Object toJSON() {
        // JSON representation is the hex-encoded imprint
        return HexConverter.encode(getImprint());
    }

    /**
     * Returns the imprint of this DataHash (algorithm + hash bytes).
     * Format: [algorithm_byte_1][algorithm_byte_2][hash_bytes...]
     */
    public byte[] getImprint() {
        byte[] imprint = new byte[hash.length + 2];
        int algValue = algorithm.getValue();
        imprint[0] = (byte) ((algValue & 0xFF00) >> 8);
        imprint[1] = (byte) (algValue & 0xFF);
        System.arraycopy(hash, 0, imprint, 2, hash.length);
        return imprint;
    }
    
    @Override
    public byte[] toCBOR() {
        // CBOR encoding of the imprint as a byte string
        return CborEncoder.encodeByteString(getImprint());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataHash dataHash = (DataHash) o;
        return Arrays.equals(hash, dataHash.hash) && algorithm == dataHash.algorithm;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(hash);
        result = 31 * result + (algorithm != null ? algorithm.hashCode() : 0);
        return result;
    }
    
    /**
     * Deserialize DataHash from CBOR.
     * The CBOR format is a byte string containing the imprint (algorithm prefix + hash).
     * 
     * @param cbor The CBOR-encoded bytes
     * @return A new DataHash instance
     */
    public static DataHash fromCBOR(byte[] cbor) {
        try {
            CustomCborDecoder.DecodeResult result = CustomCborDecoder.decode(cbor, 0);
            if (!(result.value instanceof byte[])) {
                throw new RuntimeException("Expected byte string for DataHash");
            }
            byte[] imprint = (byte[]) result.value;
            return fromImprint(imprint);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize DataHash from CBOR", e);
        }
    }
}
