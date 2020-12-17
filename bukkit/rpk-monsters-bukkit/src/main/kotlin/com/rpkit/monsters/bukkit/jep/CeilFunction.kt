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
package com.rpkit.monsters.bukkit.jep

import org.nfunk.jep.ParseException
import org.nfunk.jep.function.PostfixMathCommand
import java.util.*
import kotlin.math.ceil

class CeilFunction : PostfixMathCommand() {

    init {
        numberOfParameters = 1
    }

    override fun run(stack: Stack<Any>) {
        checkStack(stack)
        val value = stack.pop() as? Double ?: throw ParseException("Invalid parameter type")
        stack.push(ceil(value))
    }

}