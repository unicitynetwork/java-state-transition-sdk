
package com.unicity.sdk.token;

import com.unicity.sdk.ISerializable;

public class NameTagTokenData implements ISerializable {
    @Override
    public Object toJSON() {
        return null;
    }

    @Override
    public byte[] toCBOR() {
        return new byte[0];
    }
}
