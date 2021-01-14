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

package com.rpkit.core.expression.function.string

import com.rpkit.core.expression.RPKFunction

internal class SubstringFunction : RPKFunction {
    override fun invoke(vararg args: Any?): Any? {
        val arg1 = args[0].toString()
        val arg2 = args[1] as? Int ?: args[1].toString().toIntOrNull() ?: return null
        val arg3 = args[2] as? Int ?: args[2].toString().toIntOrNull() ?: arg1.length
        return arg1.substring(arg2 until arg3)
    }
}