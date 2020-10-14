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

import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import org.bukkit.World
import org.bukkit.block.Block
import kotlin.math.max
import kotlin.math.min

interface RPKSelection {

    val minecraftProfile: RPKMinecraftProfile
    var world: World
    val minimumPoint: Block
        get() = world.getBlockAt(
                min(point1.x, point2.x),
                min(point1.y, point2.y),
                min(point1.z, point2.z)
        )

    val maximumPoint: Block
        get() = world.getBlockAt(
                max(point1.x, point2.x),
                max(point1.y, point2.y),
                max(point1.z, point2.z)
        )

    var point1: Block
    var point2: Block
    val blocks: Iterable<Block>
        get() {
            val blocks = mutableListOf<Block>()
            for (x in minimumPoint.x..maximumPoint.x) {
                for (y in minimumPoint.y..maximumPoint.y) {
                    for (z in minimumPoint.z..maximumPoint.z) {
                        val block = world.getBlockAt(x, y, z)
                        if (contains(block)) {
                            blocks.add(block)
                        }
                    }
                }
            }
            return blocks
        }

    fun contains(block: Block): Boolean

}
