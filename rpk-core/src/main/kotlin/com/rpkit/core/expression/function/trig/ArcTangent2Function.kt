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

package com.rpkit.core.expression.function.trig

import com.rpkit.core.expression.RPKFunction
import kotlin.math.atan2

internal class ArcTangent2Function : RPKFunction {
    override fun invoke(vararg args: Any?): Any? {
        val arg1 = (args[0] as? Number)?.toDouble() ?: args[0].toString().toDoubleOrNull() ?: return null
        val arg2 = (args[1] as? Number)?.toDouble() ?: args[1].toString().toDoubleOrNull() ?: return null
        return atan2(arg1, arg2)
    }
}