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

package com.rpkit.selection.bukkit.listener

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.selection.bukkit.RPKSelectionBukkit
import com.rpkit.selection.bukkit.selection.RPKSelectionService
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot


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
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitPlayer) ?: return
            val selection = selectionService.getSelection(minecraftProfile)
            val clickedBlock = event.clickedBlock ?: return
            when (event.action) {
                Action.LEFT_CLICK_BLOCK -> {
                    selection.world = clickedBlock.world
                    selection.point1 = clickedBlock
                    selectionService.updateSelection(selection)
                    event.player.sendMessage(plugin.messages["wand-primary", mapOf(
                            Pair("world", clickedBlock.world.name),
                            Pair("x", clickedBlock.x.toString()),
                            Pair("y", clickedBlock.y.toString()),
                            Pair("z", clickedBlock.z.toString())
                    )])
                }
                Action.RIGHT_CLICK_BLOCK -> {
                    if (event.hand == EquipmentSlot.HAND) {
                        selection.world = clickedBlock.world
                        selection.point2 = clickedBlock
                        selectionService.updateSelection(selection)
                        event.player.sendMessage(plugin.messages["wand-secondary", mapOf(
                            "world" to clickedBlock.world.name,
                            "x" to clickedBlock.x.toString(),
                            "y" to clickedBlock.y.toString(),
                            "z" to clickedBlock.z.toString()
                        )])
                    }
                }
                else -> return
            }
        }
    }

}