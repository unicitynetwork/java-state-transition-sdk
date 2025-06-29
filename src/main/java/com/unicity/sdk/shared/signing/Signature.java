
package com.unicity.sdk.shared.signing;

import com.unicity.sdk.ISerializable;
import com.unicity.sdk.shared.cbor.CborEncoder;
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
}
