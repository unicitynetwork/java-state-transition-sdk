
package com.unicity.sdk.shared.signing;

import java.util.Arrays;

public class Signature implements ISignature {
    private final byte[] bytes;

    public Signature(byte[] bytes) {
        this.bytes = Arrays.copyOf(bytes, bytes.length);
    }

    @Override
    public byte[] getBytes() {
        return Arrays.copyOf(bytes, bytes.length);
    }
}
