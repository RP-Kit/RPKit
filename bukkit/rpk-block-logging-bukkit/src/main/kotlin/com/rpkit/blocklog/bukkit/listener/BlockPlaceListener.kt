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

package com.rpkit.blocklog.bukkit.listener

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.block.RPKBlockChangeImpl
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryService
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.bukkit.location.toRPKBlockLocation
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockPlaceEvent
import java.time.LocalDateTime


class BlockPlaceListener(private val plugin: RPKBlockLoggingBukkit) : Listener {

    @EventHandler(priority = MONITOR)
    fun onBlockPlace(event: BlockPlaceEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val blockHistoryService = Services[RPKBlockHistoryService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(event.player)
        val profile = minecraftProfile?.profile as? RPKProfile
        val character = if (minecraftProfile == null) null else characterService.getPreloadedActiveCharacter(minecraftProfile)
        blockHistoryService.getBlockHistory(event.block.toRPKBlockLocation()).thenAccept { blockHistory ->
            val blockChange = RPKBlockChangeImpl(
                blockHistory = blockHistory,
                time = LocalDateTime.now(),
                profile = profile,
                minecraftProfile = minecraftProfile,
                character = character,
                from = event.blockReplacedState.type,
                to = event.block.type,
                reason = "PLACE"
            )
            plugin.server.scheduler.runTask(plugin, Runnable {
                blockHistoryService.addBlockChange(blockChange)
            })
        }
    }

}