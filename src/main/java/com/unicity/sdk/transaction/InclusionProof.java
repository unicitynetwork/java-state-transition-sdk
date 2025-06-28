
package com.unicity.sdk.transaction;

import com.unicity.sdk.ISerializable;

public class InclusionProof implements ISerializable {
    @Override
    public Object toJSON() {
        return null;
    }

    @Override
    public byte[] toCBOR() {
        return new byte[0];
    }
}
