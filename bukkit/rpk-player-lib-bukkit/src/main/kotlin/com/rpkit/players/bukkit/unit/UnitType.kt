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

package com.rpkit.players.bukkit.unit

class UnitType(private val toString: String, val displayName: String) {
    companion object {
        @JvmStatic val HEIGHT = UnitType("HEIGHT", "Height")
        @JvmStatic val WEIGHT = UnitType("WEIGHT", "Weight")
        @JvmStatic val LONG_DISTANCE = UnitType("LONG_DISTANCE", "Long distance")
        @JvmStatic val SHORT_DISTANCE = UnitType("SHORT_DISTANCE", "Short distance")
        @JvmStatic fun values() = listOf(HEIGHT, WEIGHT, LONG_DISTANCE, SHORT_DISTANCE)
        @JvmStatic fun valueOf(value: String) = when (value) {
            "HEIGHT" -> HEIGHT
            "WEIGHT" -> WEIGHT
            "LONG_DISTANCE" -> LONG_DISTANCE
            "SHORT_DISTANCE" -> SHORT_DISTANCE
            else -> throw IllegalArgumentException("No unit type with the given name")
        }
    }

    override fun toString(): String {
        return toString
    }
}