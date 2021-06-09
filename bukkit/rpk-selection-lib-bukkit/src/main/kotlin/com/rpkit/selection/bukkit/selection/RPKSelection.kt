/*
 * Copyright 2020 Ren Binden
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

package com.rpkit.selection.bukkit.selection

import com.rpkit.core.location.RPKBlockLocation
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import kotlin.math.max
import kotlin.math.min

interface RPKSelection {

    val minecraftProfile: RPKMinecraftProfile
    var world: String
    val minimumPoint: RPKBlockLocation
        get() = RPKBlockLocation(
            world,
            min(point1.x, point2.x),
            min(point1.y, point2.y),
            min(point1.z, point2.z)
        )

    val maximumPoint: RPKBlockLocation
        get() = RPKBlockLocation(
            world,
            max(point1.x, point2.x),
            max(point1.y, point2.y),
            max(point1.z, point2.z)
        )

    var point1: RPKBlockLocation
    var point2: RPKBlockLocation
    val blocks: Iterable<RPKBlockLocation>
        get() {
            val blocks = mutableListOf<RPKBlockLocation>()
            for (x in minimumPoint.x..maximumPoint.x) {
                for (y in minimumPoint.y..maximumPoint.y) {
                    for (z in minimumPoint.z..maximumPoint.z) {
                        val block = RPKBlockLocation(world, x, y, z)
                        if (contains(block)) {
                            blocks.add(block)
                        }
                    }
                }
            }
            return blocks
        }

    fun contains(block: RPKBlockLocation): Boolean

}
