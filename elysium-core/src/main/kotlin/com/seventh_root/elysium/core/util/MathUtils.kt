package com.seventh_root.elysium.core.util

object MathUtils {

    fun fastSqrt(number: Double): Double {
        return java.lang.Double.longBitsToDouble((java.lang.Double.doubleToLongBits(number) - (1L shl 52) shr 1) + (1L shl 61))
    }

}
