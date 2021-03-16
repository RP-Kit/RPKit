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

package com.rpkit.shops.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.shops.bukkit.RPKShopsBukkit
import com.rpkit.shops.bukkit.shopcount.RPKShopCountService
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
class BlockBreakListener(val plugin: RPKShopsBukkit) : Listener {

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
                val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                if (minecraftProfileService == null) {
                    event.isCancelled = true
                    return
                }
                val characterService = Services[RPKCharacterService::class.java]
                if (characterService == null) {
                    event.isCancelled = true
                    return
                }
                val shopCountService = Services[RPKShopCountService::class.java]
                if (shopCountService == null) {
                    event.isCancelled = true
                    return
                }
                val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player)
                if (minecraftProfile == null) {
                    event.isCancelled = true
                    return
                }
                val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
                val shopCharacter = if (sign.getLine(3).equals("admin", ignoreCase = true)) {
                    null
                } else {
                    // If the person owning the shop is offline, this will return null & the event will be cancelled.
                    // This is the behaviour we expect because the only case in which the sign could be broken is if
                    // the person breaking it is the person who owns it, in which case they are online anyway.
                    // The other case in which this returns null is if the character has been deleted.
                    // In this case, the shop might as well be removed by anyone since it won't work anyway.
                    characterService.getPreloadedCharacter(RPKCharacterId(sign.getLine(3).toInt()))
                }
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
                val shopCount = shopCountService.getShopCount(character)
                shopCountService.setShopCount(character, shopCount - 1)
            } else if (sign.getLine(0) == "$GREEN[rent]") {
                val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                if (minecraftProfileService == null) {
                    event.isCancelled = true
                    return
                }
                val characterService = Services[RPKCharacterService::class.java]
                if (characterService == null) {
                    event.isCancelled = true
                    return
                }
                val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player)
                if (minecraftProfile == null) {
                    event.isCancelled = true
                    return
                }
                val character = characterService.getPreloadedActiveCharacter(minecraftProfile)
                val rentCharacter = characterService.getPreloadedCharacter(RPKCharacterId(sign.getLine(1).toInt()))
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