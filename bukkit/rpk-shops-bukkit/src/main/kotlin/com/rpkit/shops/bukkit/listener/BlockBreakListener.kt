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

package com.rpkit.shops.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.shops.bukkit.RPKShopsBukkit
import com.rpkit.shops.bukkit.shopcount.RPKShopCountProvider
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
class BlockBreakListener(val plugin: RPKShopsBukkit): Listener {

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
        if (sign != null) {
            if (sign.getLine(0) == "$GREEN[shop]") {
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val shopCountProvider = plugin.core.serviceManager.getServiceProvider(RPKShopCountProvider::class)
                val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(event.player)
                if (minecraftProfile == null) {
                    event.isCancelled = true
                    return
                }
                val character = characterProvider.getActiveCharacter(minecraftProfile)
                val shopCharacter = if (sign.getLine(3).equals("admin", ignoreCase = true)) null else characterProvider.getCharacter(sign.getLine(3).toInt())
                if (character == null) {
                    event.isCancelled = true
                    return
                }
                if (shopCharacter == null) {
                    event.isCancelled = true
                    return
                }
                if (shopCharacter.id != character.id) {
                    event.isCancelled = true
                    return
                }
                val shopCount = shopCountProvider.getShopCount(character)
                shopCountProvider.setShopCount(character, shopCount - 1)
            } else if (sign.getLine(0) == "$GREEN[rent]") {
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(event.player)
                if (minecraftProfile == null) {
                    event.isCancelled = true
                    return
                }
                val character = characterProvider.getActiveCharacter(minecraftProfile)
                val rentCharacter = characterProvider.getCharacter(sign.getLine(1).toInt())
                if (character == null) {
                    event.isCancelled = true
                    return
                }
                if (rentCharacter == null) {
                    event.isCancelled = true
                    return
                }
                if (rentCharacter.id != character.id) {
                    event.isCancelled = true
                    return
                }
            }
        }
    }

}