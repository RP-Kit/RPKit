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

enum class ShortDistanceUnit(override val displayName: String, override val scaleFactor: Double) : MeasurementUnit {

    METRES("Metres", 100000.0),
    CENTIMETRES("Centimetres", 10000000.0),
    FEET("Feet", 328084.0),
    INCHES("Inches", 3937008.0);

    override fun parse(value: String) = when (this) {
        METRES -> parseMetres(value)
        CENTIMETRES -> parseCentimetres(value)
        FEET -> parseFeet(value)
        INCHES -> parseInches(value)
    }

    private fun parseMetres(value: String): Double? {
        val regex = Regex("(?:([\\d.]+)m)?\\s*(?:([\\d.]+)cm)?")
        val result = regex.matchEntire(value) ?: return null
        val metres = result.groupValues[1].toDoubleOrNull() ?: 0.0
        val centimetres = result.groupValues[2].toDoubleOrNull() ?: 0.0
        return (metres / METRES.scaleFactor) + (centimetres / CENTIMETRES.scaleFactor)
    }

    private fun parseCentimetres(value: String): Double? = parseMetres(value)

    private fun parseFeet(value: String): Double? {
        val regex = Regex("(?:(\\d+)')?\\s*(?:(\\d+)\")?")
        val result = regex.matchEntire(value) ?: return null
        val feet = result.groupValues[1].toIntOrNull()?.toDouble() ?: 0.0
        val inches = result.groupValues[2].toIntOrNull()?.toDouble() ?: 0.0
        return (feet / FEET.scaleFactor) + (inches / INCHES.scaleFactor)
    }

    private fun parseInches(value: String): Double? = parseFeet(value)

}