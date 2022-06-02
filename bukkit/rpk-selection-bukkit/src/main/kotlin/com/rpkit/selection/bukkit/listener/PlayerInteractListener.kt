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

package com.rpkit.selection.bukkit.listener

import com.rpkit.core.bukkit.location.toRPKBlockLocation
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.selection.bukkit.RPKSelectionBukkit
import com.rpkit.selection.bukkit.selection.RPKSelectionService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action.LEFT_CLICK_BLOCK
import org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class PlayerInteractListener(private val plugin: RPKSelectionBukkit) : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val bukkitPlayer = event.player
        if (bukkitPlayer.inventory.itemInMainHand.isSimilar(plugin.config.getItemStack("wand-item"))) {
            event.isCancelled = true
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
            if (minecraftProfileService == null) {
                bukkitPlayer.sendMessage(plugin.messages["no-minecraft-profile-service"])
                return
            }
            val selectionService = Services[RPKSelectionService::class.java]
            if (selectionService == null) {
                bukkitPlayer.sendMessage(plugin.messages["no-selection-service"])
                return
            }
            val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(bukkitPlayer) ?: return
            val clickedBlock = event.clickedBlock ?: return
            val action = event.action
            selectionService.getSelection(minecraftProfile).thenAccept { existingSelection ->
                plugin.server.scheduler.runTask(plugin, Runnable {
                    if (existingSelection == null) {
                        selectionService.createSelection(minecraftProfile, bukkitPlayer.world)
                    } else {
                        CompletableFuture.completedFuture(existingSelection)
                    }.thenAccept getSelection@{ selection ->
                        when (action) {
                            LEFT_CLICK_BLOCK -> {
                                selection.world = clickedBlock.world.name
                                selection.point1 = clickedBlock.toRPKBlockLocation()
                                selectionService.updateSelection(selection).thenRun {
                                    event.player.sendMessage(
                                        plugin.messages["wand-primary", mapOf(
                                            Pair("world", clickedBlock.world.name),
                                            Pair("x", clickedBlock.x.toString()),
                                            Pair("y", clickedBlock.y.toString()),
                                            Pair("z", clickedBlock.z.toString())
                                        )]
                                    )
                                }
                            }
                            RIGHT_CLICK_BLOCK -> {
                                if (event.hand == EquipmentSlot.HAND) {
                                    selection.world = clickedBlock.world.name
                                    selection.point2 = clickedBlock.toRPKBlockLocation()
                                    selectionService.updateSelection(selection).thenRun {
                                        event.player.sendMessage(
                                            plugin.messages["wand-secondary", mapOf(
                                                "world" to clickedBlock.world.name,
                                                "x" to clickedBlock.x.toString(),
                                                "y" to clickedBlock.y.toString(),
                                                "z" to clickedBlock.z.toString()
                                            )]
                                        )
                                    }
                                }
                            }
                            else -> return@getSelection
                        }
                    }.exceptionally { exception ->
                        plugin.logger.log(Level.SEVERE, "Failed to update selection point", exception)
                        throw exception
                    }
                })
            }

        }
    }

}