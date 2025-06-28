
package com.unicity.sdk.token.fungible;

import com.unicity.sdk.ISerializable;

public class SplitMintReason implements ISerializable {
    @Override
    public Object toJSON() {
        return null;
    }

    @Override
    public byte[] toCBOR() {
        return new byte[0];
    }
}
