
package com.unicity.sdk.shared.hash;

import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.cbor.CustomCborDecoder;
import com.unicity.sdk.shared.util.HexConverter;

import java.util.Arrays;

public class DataHash {
    private final byte[] data;
    private final HashAlgorithm algorithm;

    public DataHash(HashAlgorithm algorithm, byte[] data) {
        if (algorithm == null) {
            throw new IllegalArgumentException("Invalid algorithm: null");
        }

        if (data == null) {
            throw new IllegalArgumentException("Invalid hash: null");
        }
        this.data = Arrays.copyOf(data, data.length);
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
        
        return new DataHash(algorithm, hashData);
    }
    
    /**
     * Create DataHash from JSON representation.
     * Expects a hex string in imprint format (algorithm prefix + hash).
     */
    public static DataHash fromJSON(String imprintHex) {
        return fromImprint(HexConverter.decode(imprintHex));
    }

    public byte[] getData() {
        return Arrays.copyOf(this.data, this.data.length);
    }

    public HashAlgorithm getAlgorithm() {
        return this.algorithm;
    }

    /**
     * Returns the imprint of this DataHash (algorithm + hash bytes).
     * Format: [algorithm_byte_1][algorithm_byte_2][hash_bytes...]
     */
    public byte[] getImprint() {
        byte[] imprint = new byte[this.data.length + 2];
        int algorithmValue = this.algorithm.getValue();
        imprint[0] = (byte) ((algorithmValue & 0xFF00) >> 8);
        imprint[1] = (byte) (algorithmValue & 0xFF);
        System.arraycopy(this.data, 0, imprint, 2, this.data.length);

        return imprint;
    }

    public byte[] toCBOR() {
        // CBOR encoding of the imprint as a byte string
        return CborEncoder.encodeByteString(getImprint());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataHash dataHash = (DataHash) o;
        return Arrays.equals(this.data, dataHash.data) && this.algorithm == dataHash.algorithm;
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(this.data);
        result = 31 * result + (this.algorithm != null ? this.algorithm.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return HexConverter.encode(this.getImprint());
    }
}
