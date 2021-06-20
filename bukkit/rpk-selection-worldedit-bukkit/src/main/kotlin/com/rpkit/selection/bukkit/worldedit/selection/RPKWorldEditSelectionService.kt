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
import com.rpkit.selection.bukkit.selection.RPKSelectionService
import com.rpkit.selection.bukkit.worldedit.RPKSelectionWorldEditBukkit
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import org.bukkit.World
import java.util.concurrent.CompletableFuture


class RPKWorldEditSelectionService(override val plugin: RPKSelectionWorldEditBukkit) : RPKSelectionService {

    override fun getSelection(minecraftProfile: RPKMinecraftProfile): CompletableFuture<RPKSelection?> {
        val worldEdit = plugin.server.pluginManager.getPlugin("WorldEdit") as WorldEditPlugin
        val bukkitPlayer = plugin.server.getPlayer(minecraftProfile.minecraftUUID)
                ?: return CompletableFuture.completedFuture(RPKWorldEditSelection(minecraftProfile, null))
        val session = worldEdit.getSession(bukkitPlayer)
        return CompletableFuture.completedFuture(RPKWorldEditSelection(minecraftProfile, session))
    }

    override fun createSelection(minecraftProfile: RPKMinecraftProfile, world: World): CompletableFuture<RPKSelection> {
        val worldEdit = plugin.server.pluginManager.getPlugin("WorldEdit") as WorldEditPlugin
        val bukkitPlayer = plugin.server.getPlayer(minecraftProfile.minecraftUUID)
        return CompletableFuture.completedFuture(RPKWorldEditSelection(minecraftProfile, worldEdit.getSession(bukkitPlayer)))
    }

    override fun updateSelection(selection: RPKSelection): CompletableFuture<Void> {
        return CompletableFuture.completedFuture(null)
    }

}