/*
 * Copyright 2021 Ren Binden
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

package com.rpkit.core.expression.function.rounding

import com.rpkit.core.expression.RPKFunction
import java.math.RoundingMode.HALF_UP
import java.text.DecimalFormat
import kotlin.math.round

internal class RoundFunction : RPKFunction {
    override fun invoke(vararg args: Any?): Any? {
        val arg1 = (args[0] as? Number)?.toDouble() ?: args[0].toString().toDoubleOrNull() ?: return null
        val arg2 = (args[1] as? Number)?.toInt() ?: args[1].toString().toIntOrNull() ?: 0
        if (arg2 == 0) return round(arg1)
        val decimalFormat = DecimalFormat("#.${"#".repeat(arg2)}")
        decimalFormat.roundingMode = HALF_UP
        return decimalFormat.format(arg1).toDoubleOrNull()
    }
}