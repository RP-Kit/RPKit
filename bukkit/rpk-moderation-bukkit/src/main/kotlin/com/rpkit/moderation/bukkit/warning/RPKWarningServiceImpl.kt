/*
 * Copyright 2021 Ren Binden
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

package com.rpkit.moderation.bukkit.warning

import com.rpkit.core.service.Services
import com.rpkit.moderation.bukkit.RPKModerationBukkit
import com.rpkit.moderation.bukkit.database.table.RPKWarningTable
import com.rpkit.moderation.bukkit.event.warning.RPKBukkitWarningCreateEvent
import com.rpkit.moderation.bukkit.event.warning.RPKBukkitWarningDeleteEvent
import com.rpkit.moderation.bukkit.event.warning.RPKBukkitWarningUpdateEvent
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture


class RPKWarningServiceImpl(override val plugin: RPKModerationBukkit) : RPKWarningService {

    override fun getWarning(id: RPKWarningId): CompletableFuture<RPKWarning?> {
        return plugin.database.getTable(RPKWarningTable::class.java)[id]
    }

    override fun getWarnings(profile: RPKProfile): CompletableFuture<List<RPKWarning>> {
        return plugin.database.getTable(RPKWarningTable::class.java).get(profile)
    }

    override fun addWarning(warning: RPKWarning): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitWarningCreateEvent(warning, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            val warningTable = plugin.database.getTable(RPKWarningTable::class.java)
            warningTable.insert(event.warning).join()
            // After adding warnings we want to execute any commands for that amount of warnings
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return@runAsync
            for (minecraftProfile in minecraftProfileService.getMinecraftProfiles(event.warning.profile).join()) {
                val warningCount = warningTable.get(event.warning.profile).join().size
                plugin.server.scheduler.runTask(plugin, Runnable {
                    val command = plugin.config.getString("warnings.${warningCount}")
                        ?.replace("\${player}", minecraftProfile.name)
                    if (command != null) {
                        plugin.server.dispatchCommand(plugin.server.consoleSender, command)
                    }
                })
            }
        }
    }

    override fun createWarning(
        reason: String,
        profile: RPKProfile,
        issuer: RPKProfile,
        time: LocalDateTime
    ): CompletableFuture<RPKWarning> {
        val warning = RPKWarningImpl(
            null,
            reason,
            profile,
            issuer,
            time
        )
        return addWarning(warning).thenApply { warning }
    }

    override fun removeWarning(warning: RPKWarning): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitWarningDeleteEvent(warning, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            plugin.database.getTable(RPKWarningTable::class.java).delete(event.warning).join()
        }
    }

    override fun updateWarning(warning: RPKWarning): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitWarningUpdateEvent(warning, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            plugin.database.getTable(RPKWarningTable::class.java).update(event.warning).join()
        }
    }

}