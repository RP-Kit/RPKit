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

enum class WeightUnit(override val displayName: String, override val scaleFactor: Double) : MeasurementUnit {

    KILOGRAMS("Kilograms", 1000000.0),
    GRAMS("Grams", 1000000000.0),
    STONE("Stone", 157473.0),
    POUNDS("Pounds",  2204622.0);

    override fun parse(value: String): Double? = when (this) {
        KILOGRAMS -> parseKilograms(value)
        GRAMS -> parseGrams(value)
        STONE -> parseStone(value)
        POUNDS -> parsePounds(value)
    }

    private fun parseKilograms(value: String): Double? {
        val regex = Regex("(?:([\\d.]+)kg)?\\s*(?:([\\d.]+)g)?")
        val result = regex.matchEntire(value) ?: return null
        val kilograms = result.groupValues[1].toDoubleOrNull() ?: 0.0
        val grams = result.groupValues[2].toDoubleOrNull() ?: 0.0
        return (kilograms / KILOGRAMS.scaleFactor) + (grams / GRAMS.scaleFactor)
    }

    private fun parseGrams(value: String) = parseKilograms(value)

    private fun parseStone(value: String): Double? {
        val regex = Regex("(?:([\\d.]+)st)?\\s*(?:([\\d.]+)lb)?")
        val result = regex.matchEntire(value) ?: return null
        val stone = result.groupValues[1].toDoubleOrNull() ?: 0.0
        val pounds = result.groupValues[2].toDoubleOrNull() ?: 0.0
        return (stone / STONE.scaleFactor) + (pounds / POUNDS.scaleFactor)
    }

    private fun parsePounds(value: String) = parseStone(value)

}