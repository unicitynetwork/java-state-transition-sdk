
package com.unicity.sdk.identity;


import com.unicity.sdk.shared.util.HexConverter;

import java.util.Arrays;

public class PublicKeyIdentity implements IIdentity {
    private final byte[] publicKey;

    public PublicKeyIdentity(byte[] publicKey) {
        this.publicKey = Arrays.copyOf(publicKey, publicKey.length);
    }

    public byte[] getPublicKey() {
        return Arrays.copyOf(publicKey, publicKey.length);
    }

    @Override
    public Object toJSON() {
        return HexConverter.encode(publicKey);
    }

    @Override
    public byte[] toCBOR() {
        return publicKey;
    }
}
