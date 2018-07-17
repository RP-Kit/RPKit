/*
 * Copyright 2018 Ross Binden
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

package com.rpkit.selection.bukkit.worldedit.selection

import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.selection.bukkit.selection.RPKSelection
import com.sk89q.worldedit.bukkit.BukkitUtil
import com.sk89q.worldedit.bukkit.selections.Selection
import com.sk89q.worldedit.regions.selector.limit.PermissiveSelectorLimits
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.Block


class RPKWorldEditSelection(
        override var id: Int = 0,
        override val minecraftProfile: RPKMinecraftProfile,
        val selection: Selection?
): RPKSelection {

    override var point1: Block
        get() {
            if (selection == null) {
                val world = Bukkit.getWorlds()[0]
                return world.getBlockAt(world.spawnLocation)
            } else {
                val block = selection.world?.getBlockAt(
                        selection.nativeMinimumPoint.blockX,
                        selection.nativeMinimumPoint.blockY,
                        selection.nativeMinimumPoint.blockZ
                )
                if (block != null) return block
                val world = Bukkit.getWorlds()[0]
                return world.getBlockAt(world.spawnLocation)
            }
        }
        set(value) {
            selection?.regionSelector?.selectPrimary(BukkitUtil.toVector(value), PermissiveSelectorLimits.getInstance())
        }

    override var point2: Block
        get() {
            if (selection == null) {
                val world = Bukkit.getWorlds()[0]
                return world.getBlockAt(world.spawnLocation)
            } else {
                val block = selection.world?.getBlockAt(
                        selection.nativeMaximumPoint.blockX,
                        selection.nativeMaximumPoint.blockY,
                        selection.nativeMaximumPoint.blockZ
                )
                if (block != null) return block
                val world = Bukkit.getServer().worlds[0]
                return world.getBlockAt(world.spawnLocation)
            }
        }
        set(value) {
            selection?.regionSelector?.selectSecondary(BukkitUtil.toVector(value), PermissiveSelectorLimits.getInstance())
        }

    override var world: World
        get() = selection?.world ?: Bukkit.getWorlds()[0]
        set(value) {
            selection?.regionSelector?.world = BukkitUtil.getLocalWorld(value)
        }

    override fun contains(block: Block): Boolean {
        return selection?.contains(block.location) ?: false
    }

}