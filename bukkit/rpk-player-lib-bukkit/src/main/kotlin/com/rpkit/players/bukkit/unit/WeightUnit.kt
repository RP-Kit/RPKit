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

    KILOGRAMS("Kilograms", 157473.0),
    GRAMS("Grams", 157473000.0),
    STONE("Stone", 1000000.0),
    POUNDS("Pounds", 14000000.0)

}