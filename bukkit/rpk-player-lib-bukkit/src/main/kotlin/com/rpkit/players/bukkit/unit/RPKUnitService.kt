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

import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.RPKProfileId
import java.util.concurrent.CompletableFuture

interface RPKUnitService : Service {

    fun getDefaultUnits(type: UnitType): MeasurementUnit
    fun getPreferredUnit(profileId: RPKProfileId, type: UnitType): CompletableFuture<MeasurementUnit>
    fun setPreferredUnit(profileId: RPKProfileId, type: UnitType, unit: MeasurementUnit): CompletableFuture<Void>
    fun <T: MeasurementUnit> convert(amount: Double, from: T, to: T): Double
    fun format(amount: Double, unit: MeasurementUnit): String

}