package com.unicity.sdk.address;

import com.unicity.sdk.identity.IIdentity;
import com.unicity.sdk.util.HexConverter;

import java.util.Arrays;

public class DirectAddress implements IAddress {
    private final String address;

    private DirectAddress(String address) {
        this.address = address;
    }

    public static DirectAddress create(IIdentity identity) {
        byte[] identityBytes = identity.toCBOR();
        byte checksum = 0;
        for (byte b : identityBytes) {
            checksum ^= b;
        }
        String address = AddressScheme.DIRECT.getScheme() + "://" + HexConverter.encode(identityBytes) + HexConverter.encode(new byte[]{checksum});
        return new DirectAddress(address);
    }

    public static DirectAddress fromString(String address) {
        if (!address.startsWith(AddressScheme.DIRECT.getScheme() + "://")) {
            throw new IllegalArgumentException("Invalid direct address scheme");
        }
        String[] parts = address.substring(AddressScheme.DIRECT.getScheme().length() + 3).split("(?<=\\G.{2})");
        byte[] identityBytes = HexConverter.decode(String.join("", Arrays.copyOfRange(parts, 0, parts.length - 1)));
        byte checksum = HexConverter.decode(parts[parts.length - 1])[0];

        byte calculatedChecksum = 0;
        for (byte b : identityBytes) {
            calculatedChecksum ^= b;
        }

        if (checksum != calculatedChecksum) {
            throw new IllegalArgumentException("Invalid checksum");
        }

        return new DirectAddress(address);
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public Object toJSON() {
        return address;
    }

    @Override
    public byte[] toCBOR() {
        return address.getBytes();
    }
}