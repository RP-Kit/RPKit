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

import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.selection.bukkit.RPKSelectionBukkit
import com.rpkit.selection.bukkit.database.table.RPKSelectionTable


class RPKSelectionServiceImpl(override val plugin: RPKSelectionBukkit) : RPKSelectionService {

    override fun getSelection(minecraftProfile: RPKMinecraftProfile): RPKSelection {
        var selection = plugin.database.getTable(RPKSelectionTable::class.java).get(minecraftProfile)
        if (selection != null) return selection
        val bukkitPlayer = plugin.server.getPlayer(minecraftProfile.minecraftUUID)
                ?: throw IllegalArgumentException("Invalid Minecraft profile")
        val world = bukkitPlayer.world
        selection = RPKSelectionImpl(minecraftProfile, world, world.getBlockAt(world.spawnLocation), world.getBlockAt(world.spawnLocation))
        plugin.database.getTable(RPKSelectionTable::class.java).insert(selection)
        return selection
    }

    override fun updateSelection(selection: RPKSelection) {
        plugin.database.getTable(RPKSelectionTable::class.java).update(selection)
    }

}