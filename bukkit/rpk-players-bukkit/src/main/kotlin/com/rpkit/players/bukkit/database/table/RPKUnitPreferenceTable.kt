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

package com.rpkit.players.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.players.bukkit.database.create
import com.rpkit.players.bukkit.database.jooq.Tables.RPKIT_UNIT_PREFERENCE
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.unit.*
import com.rpkit.players.bukkit.unit.UnitType.Companion.HEIGHT
import com.rpkit.players.bukkit.unit.UnitType.Companion.LONG_DISTANCE
import com.rpkit.players.bukkit.unit.UnitType.Companion.SHORT_DISTANCE
import com.rpkit.players.bukkit.unit.UnitType.Companion.WEIGHT
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.concurrent.CompletableFuture.supplyAsync

class RPKUnitPreferenceTable(private val database: Database) : Table {

    fun get(profileId: RPKProfileId, type: UnitType): CompletableFuture<MeasurementUnit?> {
        return supplyAsync {
            val preference = database.create.select(RPKIT_UNIT_PREFERENCE.PREFERENCE)
                .from(RPKIT_UNIT_PREFERENCE)
                .where(RPKIT_UNIT_PREFERENCE.PROFILE_ID.eq(profileId.value))
                .and(RPKIT_UNIT_PREFERENCE.UNIT_TYPE.eq(type.toString()))
                .fetchOne()
                ?.get(RPKIT_UNIT_PREFERENCE.PREFERENCE) ?: return@supplyAsync null
            return@supplyAsync when (type) {
                HEIGHT -> HeightUnit.valueOf(preference)
                WEIGHT -> WeightUnit.valueOf(preference)
                LONG_DISTANCE -> LongDistanceUnit.valueOf(preference)
                SHORT_DISTANCE -> ShortDistanceUnit.valueOf(preference)
                else -> throw IllegalArgumentException("Unrecognised unit type")
            }
        }
    }

    fun set(profileId: RPKProfileId, type: UnitType, preference: MeasurementUnit): CompletableFuture<Void> {
        return runAsync {
            database.create
                .insertInto(RPKIT_UNIT_PREFERENCE)
                .set(RPKIT_UNIT_PREFERENCE.PROFILE_ID, profileId.value)
                .set(RPKIT_UNIT_PREFERENCE.UNIT_TYPE, type.toString())
                .set(RPKIT_UNIT_PREFERENCE.PREFERENCE, preference.toString())
                .onConflict(RPKIT_UNIT_PREFERENCE.PROFILE_ID, RPKIT_UNIT_PREFERENCE.UNIT_TYPE).doUpdate()
                .set(RPKIT_UNIT_PREFERENCE.PREFERENCE, preference.toString())
                .execute()
        }
    }

}