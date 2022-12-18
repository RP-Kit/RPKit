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

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.table.RPKUnitPreferenceTable
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.unit.UnitType.Companion.HEIGHT
import com.rpkit.players.bukkit.unit.UnitType.Companion.LONG_DISTANCE
import com.rpkit.players.bukkit.unit.UnitType.Companion.SHORT_DISTANCE
import com.rpkit.players.bukkit.unit.UnitType.Companion.WEIGHT
import java.util.concurrent.CompletableFuture
import java.util.logging.Level.SEVERE
import kotlin.math.floor
import kotlin.math.roundToInt

class RPKUnitServiceImpl(override val plugin: RPKPlayersBukkit) : RPKUnitService {
    override fun getDefaultUnits(type: UnitType): MeasurementUnit {
        return when (type) {
            HEIGHT -> plugin.config.getString("units.height.default")!!.uppercase().let(HeightUnit::valueOf)
            WEIGHT -> plugin.config.getString("units.weight.default")!!.uppercase().let(WeightUnit::valueOf)
            LONG_DISTANCE -> plugin.config.getString("units.long-distance.default")!!.uppercase().let(LongDistanceUnit::valueOf)
            SHORT_DISTANCE -> plugin.config.getString("units.short-distance.default")!!.uppercase().let(ShortDistanceUnit::valueOf)
            else -> throw IllegalArgumentException("Unrecognised unit type")
        }
    }

    override fun getPreferredUnit(profileId: RPKProfileId, type: UnitType): CompletableFuture<MeasurementUnit> {
        return plugin.database.getTable(RPKUnitPreferenceTable::class.java).get(profileId, type)
            .exceptionally { exception ->
                plugin.logger.log(SEVERE, "Failed to get preferred unit", exception)
                throw exception
            }.thenApply { preference ->
                preference ?: getDefaultUnits(type)
            }
    }

    override fun setPreferredUnit(profileId: RPKProfileId, type: UnitType, unit: MeasurementUnit): CompletableFuture<Void> {
        return plugin.database.getTable(RPKUnitPreferenceTable::class.java).set(profileId, type, unit)
            .exceptionally { exception ->
                plugin.logger.log(SEVERE, "Failed to set preferred unit", exception)
                throw exception
            }
    }

    override fun <T : MeasurementUnit> convert(amount: Double, from: T, to: T): Double {
        return (amount / from.scaleFactor) * to.scaleFactor
    }

    override fun format(amount: Double, unit: MeasurementUnit): String {
        return when (unit) {
            HeightUnit.FEET, ShortDistanceUnit.FEET -> "${floor(amount).toInt()}'${convert(amount % 1.0, HeightUnit.FEET, HeightUnit.INCHES).roundToInt()}\""
            HeightUnit.INCHES, ShortDistanceUnit.INCHES -> "${amount.roundToInt()}\""
            HeightUnit.METRES, ShortDistanceUnit.METRES -> "${floor(amount).toInt()}m ${convert(amount % 1.0, HeightUnit.METRES, HeightUnit.CENTIMETRES).roundToInt()}cm"
            HeightUnit.CENTIMETRES, ShortDistanceUnit.CENTIMETRES -> "${amount.roundToInt()}cm"
            LongDistanceUnit.KILOMETRES -> "${amount.roundToInt()}km"
            LongDistanceUnit.MILES -> "${amount.roundToInt()}mi"
            WeightUnit.KILOGRAMS -> "${floor(amount).toInt()}kg ${convert(amount % 1.0, WeightUnit.KILOGRAMS, WeightUnit.GRAMS).roundToInt()}g"
            WeightUnit.GRAMS -> "${amount.roundToInt()}g"
            WeightUnit.STONE -> "${floor(amount).toInt()}st ${convert(amount % 1.0, WeightUnit.STONE, WeightUnit.POUNDS).roundToInt()}lb"
            WeightUnit.POUNDS -> "${amount.roundToInt()}lb"
            else -> "${amount.roundToInt()}"
        }
    }
}