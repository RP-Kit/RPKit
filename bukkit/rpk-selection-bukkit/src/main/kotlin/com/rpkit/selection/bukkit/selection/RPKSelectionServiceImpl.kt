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

import com.rpkit.core.bukkit.location.toRPKBlockLocation
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.selection.bukkit.RPKSelectionBukkit
import com.rpkit.selection.bukkit.database.table.RPKSelectionTable
import org.bukkit.World
import java.util.concurrent.CompletableFuture


class RPKSelectionServiceImpl(override val plugin: RPKSelectionBukkit) : RPKSelectionService {

    override fun getSelection(minecraftProfile: RPKMinecraftProfile): CompletableFuture<RPKSelection?> {
        return plugin.database.getTable(RPKSelectionTable::class.java)[minecraftProfile]
    }

    override fun createSelection(minecraftProfile: RPKMinecraftProfile, world: World): CompletableFuture<RPKSelection> {
        val selection = RPKSelectionImpl(
            minecraftProfile,
            world.name,
            world.spawnLocation.toRPKBlockLocation(),
            world.spawnLocation.toRPKBlockLocation()
        )
        return plugin.database.getTable(RPKSelectionTable::class.java).insert(selection).thenApply { selection }
    }

    override fun updateSelection(selection: RPKSelection): CompletableFuture<Void> {
        return plugin.database.getTable(RPKSelectionTable::class.java).update(selection)
    }

}