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

package com.rpkit.travel.bukkit.untamer

import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.travel.bukkit.database.table.RPKUntamerTable
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.CompletableFuture.runAsync
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Level.SEVERE

class RPKUntamerService(override val plugin: RPKTravelBukkit) : Service {

    private val untaming = ConcurrentHashMap<Int, Boolean>()

    fun loadUntaming(id: RPKMinecraftProfileId): CompletableFuture<Boolean> {
        val preloadedUntamingState = untaming[id.value]
        if (preloadedUntamingState != null) return completedFuture(preloadedUntamingState)
        plugin.logger.info("Loading untaming state for Minecraft profile ${id.value}...")
        val untamingStateFuture = plugin.database.getTable(RPKUntamerTable::class.java).isPresent(id)
        untamingStateFuture.thenAccept { untamingState ->
            untaming[id.value] = untamingState
            plugin.logger.info("Loaded untaming state for Minecraft profile ${id.value}")
        }
        return untamingStateFuture
    }

    fun unloadUntaming(id: RPKMinecraftProfileId) {
        untaming.remove(id.value)
        plugin.logger.info("Unloaded untaming state for Minecraft profile ${id.value}")
    }

    fun isUntaming(minecraftProfileId: RPKMinecraftProfileId): Boolean {
        return untaming[minecraftProfileId.value] ?: false
    }

    fun setUntaming(minecraftProfileId: RPKMinecraftProfileId, untaming: Boolean): CompletableFuture<Void> = runAsync {
        val table = plugin.database.getTable(RPKUntamerTable::class.java)
        val preloadedUntaming = this.untaming[minecraftProfileId.value] ?: false
        if (preloadedUntaming) {
            if (!untaming) {
                table.delete(minecraftProfileId).exceptionally { exception ->
                    plugin.logger.log(SEVERE, "Failed to set untaming", exception)
                    throw exception
                }.thenRun {
                    this.untaming[minecraftProfileId.value] = false
                }
            }
        } else {
            if (untaming) {
                table.insert(minecraftProfileId).exceptionally { exception ->
                    plugin.logger.log(SEVERE, "Failed to set untaming", exception)
                    throw exception
                }.thenRun {
                    this.untaming[minecraftProfileId.value] = true
                }
            }
        }
    }

}