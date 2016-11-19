/*
 * Copyright 2016 Ross Binden
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

package com.seventh_root.elysium.shops.bukkit.listener

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import com.seventh_root.elysium.shops.bukkit.ElysiumShopsBukkit
import com.seventh_root.elysium.shops.bukkit.shopcount.ElysiumShopCountProvider
import org.bukkit.ChatColor.GREEN
import org.bukkit.block.BlockFace.UP
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

/**
 * Block break listener for breaking shops.
 */
class BlockBreakListener(val plugin: ElysiumShopsBukkit): Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        val state = event.block.state
        var sign: Sign? = null
        if (state is Sign) {
            sign = state
        }
        if (state is Chest) {
            sign = event.block.getRelative(UP).state as? Sign
        }
        if (sign?.getLine(0) == GREEN.toString() + "[shop]") {
            val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
            val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class)
            val shopCountProvider = plugin.core.serviceManager.getServiceProvider(ElysiumShopCountProvider::class)
            val player = playerProvider.getPlayer(event.player)
            val character = characterProvider.getActiveCharacter(player)
            if (character != null) {
                val shopCount = shopCountProvider.getShopCount(character)
                shopCountProvider.setShopCount(character, shopCount - 1)
            }
        }
    }

}