/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.rolling.bukkit.roll

import com.rpkit.rolling.bukkit.roll.Roll.RollPart

class RollPartResult(val rollPart: RollPart, private val individualResults: List<Int>) {
    val result = individualResults.sum()

    override fun toString() = buildString {
        append("(")
        append(individualResults
            .map { result: Int ->
                result.toString()
            }
            .reduce { a: String, b: String -> a + (if (b.startsWith("-")) "" else "+") + b })
        append(")")
    }
}