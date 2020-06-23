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


class RPKSelectionImpl(
        override var id: Int = 0,
        override val minecraftProfile: RPKMinecraftProfile,
        override var world: World,
        override var point1: Block,
        override var point2: Block
): RPKSelection {

    override val minimumPoint: Block
        get() = world.getBlockAt(
                Math.min(point1.x, point2.x),
                Math.min(point1.y, point2.y),
                Math.min(point1.z, point2.z)
        )

    override val maximumPoint: Block
        get() = world.getBlockAt(
                Math.max(point1.x, point2.x),
                Math.max(point1.y, point2.y),
                Math.max(point1.z, point2.z)
        )

    override fun contains(block: Block): Boolean {
        return block.world == world
                && block.x >= minimumPoint.x
                && block.y >= minimumPoint.y
                && block.z >= minimumPoint.z
                && block.x <= maximumPoint.x
                && block.y <= maximumPoint.y
                && block.z <= maximumPoint.z
    }

}