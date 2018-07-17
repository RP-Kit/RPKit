/*
 * Copyright 2018 Ross Binden
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

import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.selection.bukkit.RPKSelectionBukkit
import com.rpkit.selection.bukkit.selection.RPKSelectionProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot


class PlayerInteractListener(private val plugin: RPKSelectionBukkit): Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val bukkitPlayer = event.player
        if (bukkitPlayer.inventory.itemInMainHand.isSimilar(plugin.config.getItemStack("wand-item"))) {
            event.isCancelled = true
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val selectionProvider = plugin.core.serviceManager.getServiceProvider(RPKSelectionProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer) ?: return
            val selection = selectionProvider.getSelection(minecraftProfile)
            when (event.action) {
                Action.LEFT_CLICK_BLOCK -> {
                    selection.world = event.clickedBlock.world
                    selection.point1 = event.clickedBlock
                    selectionProvider.updateSelection(selection)
                    event.player.sendMessage(plugin.messages["wand-primary", mapOf(
                            Pair("world", event.clickedBlock.world.name),
                            Pair("x", event.clickedBlock.x.toString()),
                            Pair("y", event.clickedBlock.y.toString()),
                            Pair("z", event.clickedBlock.z.toString())
                    )])
                }
                Action.RIGHT_CLICK_BLOCK -> {
                    if (event.hand == EquipmentSlot.HAND) {
                        selection.world = event.clickedBlock.world
                        selection.point2 = event.clickedBlock
                        selectionProvider.updateSelection(selection)
                        event.player.sendMessage(plugin.messages["wand-secondary", mapOf(
                                Pair("world", event.clickedBlock.world.name),
                                Pair("x", event.clickedBlock.x.toString()),
                                Pair("y", event.clickedBlock.y.toString()),
                                Pair("z", event.clickedBlock.z.toString())
                        )])
                    }
                }
                else -> return
            }
        }
    }

}