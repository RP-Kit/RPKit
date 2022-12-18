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

enum class HeightUnit(override val displayName: String, override val scaleFactor: Double) : MeasurementUnit {

    METRES("Metres", 15.0),
    CENTIMETRES("Centimetres", 1500.0),
    FEET("Feet", 50.0),
    INCHES("Inches", 600.0)

}