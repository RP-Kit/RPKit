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

enum class LongDistanceUnit(override val displayName: String, override val scaleFactor: Double) : MeasurementUnit {

    MILES("Miles", 6213712.0),
    KILOMETRES("Kilometres", 10000000.0);

    override fun parse(value: String) = when (this) {
        MILES -> parseMiles(value)
        KILOMETRES -> parseKilometres(value)
    }

    private fun parseMiles(value: String): Double? {
        val regex = Regex("([\\d.]+)mi")
        val result = regex.matchEntire(value) ?: return null
        val miles = result.groupValues[1].toDoubleOrNull() ?: 0.0
        return miles / MILES.scaleFactor
    }

    private fun parseKilometres(value: String): Double? {
        val regex = Regex("([\\d.]+)km")
        val result = regex.matchEntire(value) ?: return null
        val miles = result.groupValues[1].toDoubleOrNull() ?: 0.0
        return miles / KILOMETRES.scaleFactor
    }

}