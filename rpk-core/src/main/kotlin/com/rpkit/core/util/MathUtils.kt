/*
 * Copyright 2020 Ren Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rpkit.core.util

/**
 * Maths functions object.
 */
object MathUtils {

    /**
     * Performs a fast, but less accurate, square root operation.
     * This may be used when quickly attempting to find things like distance, where the accuracy of [Math.sqrt] is not necessarily required.
     *
     * @param number The number to square root
     * @return The approximate square root of the number.
     */
    fun fastSqrt(number: Double): Double {
        return java.lang.Double.longBitsToDouble((java.lang.Double.doubleToLongBits(number) - (1L shl 52) shr 1) + (1L shl 61))
    }

}
