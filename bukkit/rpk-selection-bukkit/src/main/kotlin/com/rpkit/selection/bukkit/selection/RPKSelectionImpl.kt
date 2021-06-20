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


class RPKSelectionImpl(
        override val minecraftProfile: RPKMinecraftProfile,
        override var world: String,
        override var point1: RPKBlockLocation,
        override var point2: RPKBlockLocation
) : RPKSelection {

    override fun contains(block: RPKBlockLocation): Boolean {
        return block.world == world
                && block.x >= minimumPoint.x
                && block.y >= minimumPoint.y
                && block.z >= minimumPoint.z
                && block.x <= maximumPoint.x
                && block.y <= maximumPoint.y
                && block.z <= maximumPoint.z
    }

}