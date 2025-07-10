
package com.unicity.sdk.shared.signing;

import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.cbor.CborDecoder;
import com.unicity.sdk.shared.cbor.CborEncoder;
import com.unicity.sdk.shared.cbor.CustomCborDecoder;
import com.unicity.sdk.shared.util.HexConverter;
import java.util.Arrays;

public class Signature implements ISignature, ISerializable {
    private final byte[] bytes;
    private final int recovery;

    public Signature(byte[] bytes, int recovery) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
        this.recovery = recovery;
    }

    @Override
    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }
    
    public int getRecovery() {
        return recovery;
    }
    
    @Override
    public byte[] toCBOR() {
        byte[] fullSignature = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, fullSignature, 0, bytes.length);
        fullSignature[bytes.length] = (byte) recovery;
        return CborEncoder.encodeByteString(fullSignature);
    }
    
    @Override
    public Object toJSON() {
        byte[] fullSignature = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, fullSignature, 0, bytes.length);
        fullSignature[bytes.length] = (byte) recovery;
        return HexConverter.encode(fullSignature);
    }
    
    /**
     * Deserialize Signature from CBOR.
     * @param cbor The CBOR-encoded bytes
     * @return A Signature instance
     */
    public static Signature fromCBOR(byte[] cbor) {
        try {
            CustomCborDecoder.DecodeResult result = CustomCborDecoder.decode(cbor, 0);
            if (!(result.value instanceof byte[])) {
                throw new RuntimeException("Expected byte string for Signature");
            }
            
            byte[] fullSignature = (byte[]) result.value;
            if (fullSignature.length < 65) {
                throw new RuntimeException("Invalid signature length: " + fullSignature.length);
            }
            
            // Extract signature bytes (first 64 bytes) and recovery byte (last byte)
            byte[] sigBytes = Arrays.copyOfRange(fullSignature, 0, 64);
            int recovery = fullSignature[64] & 0xFF;
            
            return new Signature(sigBytes, recovery);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize Signature from CBOR", e);
        }
    }
}
