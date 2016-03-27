package com.seventh_root.elysium.core.util;

public class MathUtils {

    private MathUtils() {}

    public static double fastSqrt(double number) {
        return Double.longBitsToDouble(((Double.doubleToLongBits(number) - (1L << 52)) >> 1) + (1L << 61));
    }

}
