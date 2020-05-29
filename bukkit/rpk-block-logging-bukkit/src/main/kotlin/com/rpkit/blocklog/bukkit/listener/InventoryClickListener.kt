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
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryProvider
import com.rpkit.blocklog.bukkit.block.RPKBlockInventoryChangeImpl
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.block.BlockState
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority.MONITOR
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.scheduler.BukkitRunnable


class InventoryClickListener(private val plugin: RPKBlockLoggingBukkit): Listener {

    @EventHandler(priority = MONITOR)
    fun onInventoryClick(event: InventoryClickEvent) {
        val blockHistoryProvider = plugin.core.serviceManager.getServiceProvider(RPKBlockHistoryProvider::class)
        val inventoryHolder = event.inventory.holder
        if (inventoryHolder is BlockState) {
            val whoClicked = event.whoClicked
            if (whoClicked is Player) {
                val oldContents = event.inventory.contents
                object: BukkitRunnable() {
                    override fun run() {
                        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(whoClicked)
                        val profile = minecraftProfile?.profile as? RPKProfile
                        val character = if (minecraftProfile == null) null else characterProvider.getActiveCharacter(minecraftProfile)
                        val blockHistory = blockHistoryProvider.getBlockHistory(inventoryHolder.block)
                        val blockInventoryChange = RPKBlockInventoryChangeImpl(
                                blockHistory = blockHistory,
                                time = System.currentTimeMillis(),
                                profile = profile,
                                minecraftProfile = minecraftProfile,
                                character = character,
                                from = oldContents,
                                to = event.inventory.contents,
                                reason = "CLICK"
                        )
                        blockHistoryProvider.addBlockInventoryChange(blockInventoryChange)
                    }
                }.runTaskLater(plugin, 1L) // Scheduled 1 tick later to allow inventory change to take place.
            }
        }
    }

}