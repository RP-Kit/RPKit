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

package com.rpkit.selection.bukkit.worldedit.selection

import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.selection.bukkit.selection.RPKSelection
import com.sk89q.worldedit.IncompleteRegionException
import com.sk89q.worldedit.LocalSession
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector
import com.sk89q.worldedit.regions.selector.limit.PermissiveSelectorLimits
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.block.Block
import kotlin.math.floor


class RPKWorldEditSelection(
        override val minecraftProfile: RPKMinecraftProfile,
        val session: LocalSession?
) : RPKSelection {

    override var point1: Block
        get() {
            return if (session == null) {
                val world = Bukkit.getWorlds()[0]
                world.getBlockAt(world.spawnLocation)
            } else {
                val world = BukkitAdapter.adapt(session.selectionWorld)
                val primaryPosition = try {
                    session.getRegionSelector(session.selectionWorld).primaryPosition.toVector3()
                } catch (exception: IncompleteRegionException) {
                    BukkitAdapter.adapt(world.spawnLocation).toVector()
                }
                world.getBlockAt(
                        floor(primaryPosition.x).toInt(),
                        floor(primaryPosition.y).toInt(),
                        floor(primaryPosition.z).toInt()
                )
            }
        }
        set(value) {
            val selector = session?.getRegionSelector(BukkitAdapter.adapt(value.world))
            selector?.selectPrimary(
                    BlockVector3.at(
                            value.x,
                            value.y,
                            value.z
                    ),
                    PermissiveSelectorLimits.getInstance()
            )
        }

    override var point2: Block
        get() {
            return if (session == null) {
                val world = Bukkit.getWorlds()[0]
                world.getBlockAt(world.spawnLocation)
            } else {
                val world = BukkitAdapter.adapt(session.selectionWorld)
                val secondaryPosition = try {
                    (session.getRegionSelector(session.selectionWorld) as? CuboidRegionSelector)?.region?.pos2?.toVector3()
                } catch (exception: IncompleteRegionException) {
                    null
                } ?: BukkitAdapter.adapt(world.spawnLocation).toVector()
                world.getBlockAt(
                        floor(secondaryPosition.x).toInt(),
                        floor(secondaryPosition.y).toInt(),
                        floor(secondaryPosition.z).toInt()
                )
            }
        }
        set(value) {
            val selector = session?.getRegionSelector(BukkitAdapter.adapt(value.world))
            selector?.selectPrimary(
                    BlockVector3.at(
                            value.x,
                            value.y,
                            value.z
                    ),
                    PermissiveSelectorLimits.getInstance()
            )
        }

    override var world: World
        get() = BukkitAdapter.adapt(session?.selectionWorld) ?: Bukkit.getWorlds()[0]
        set(value) {

        }

    override fun contains(block: Block): Boolean {
        return session?.getSelection(session.selectionWorld)?.contains(BukkitAdapter.asBlockVector(block.location))
                ?: false
    }

}