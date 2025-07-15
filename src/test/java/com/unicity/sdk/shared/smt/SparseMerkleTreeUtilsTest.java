package com.unicity.sdk.shared.smt;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

public class SparseMerkleTreeUtilsTest {

    @Test
    public void treeShouldBeHalfCalculated() throws Exception {
        Assertions.assertTrue(
                CommonPath.create(
                        BigInteger.valueOf(0b11),
                        BigInteger.valueOf(0b111101111)
                ).equals(
                        new CommonPath(BigInteger.valueOf(0b11), 1)
                )
        );
        Assertions.assertTrue(
                CommonPath.create(
                        BigInteger.valueOf(0b111101111),
                        BigInteger.valueOf(0b11)
                ).equals(
                        new CommonPath(BigInteger.valueOf(0b11), 1)
                )
        );
        Assertions.assertTrue(
                CommonPath.create(
                        BigInteger.valueOf(0b110010000),
                        BigInteger.valueOf(0b100010000)
                ).equals(
                        new CommonPath(BigInteger.valueOf(0b10010000), 7)
                )
        );
    }
}
