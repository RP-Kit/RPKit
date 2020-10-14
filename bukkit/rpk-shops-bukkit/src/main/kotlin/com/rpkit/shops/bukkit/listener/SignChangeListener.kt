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

package com.rpkit.shops.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileService
import com.rpkit.shops.bukkit.RPKShopsBukkit
import com.rpkit.shops.bukkit.shopcount.RPKShopCountService
import org.bukkit.ChatColor.GREEN
import org.bukkit.Material
import org.bukkit.Material.CHEST
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Sign change listener for shops.
 */
class SignChangeListener(private val plugin: RPKShopsBukkit) : Listener {

    @EventHandler
    fun onSignChange(event: SignChangeEvent) {
        if (event.getLine(0) == "[shop]") {
            if (!event.player.hasPermission("rpkit.shops.sign.shop")) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["no-permission-shop"])
                return
            }
            val minecraftProfileService = Services[RPKMinecraftProfileService::class]
            if (minecraftProfileService == null) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["no-minecraft-profile-service"])
                return
            }
            val characterService = Services[RPKCharacterService::class]
            if (characterService == null) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["no-character-service"])
                return
            }
            val currencyService = Services[RPKCurrencyService::class]
            if (currencyService == null) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["no-currency-service"])
                return
            }
            val shopCountService = Services[RPKShopCountService::class]
            if (shopCountService == null) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["no-shop-count-service"])
                return
            }
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player)
            if (minecraftProfile == null) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["no-minecraft-profile"])
                return
            }
            val character = characterService.getActiveCharacter(minecraftProfile)
            if (character == null) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["no-character"])
                return
            }
            val shopCount = shopCountService.getShopCount(character)
            if (shopCount >= plugin.config.getInt("shops.limit") && !event.player.hasPermission("rpkit.shops.sign.shop.nolimit")) {
                event.player.sendMessage(plugin.messages["no-permission-shop-limit"])
                event.block.breakNaturally()
                return
            }
            if ((event.getLine(1)?.matches(Regex("buy\\s+\\d+")) != true
                            || (event.getLine(1)?.matches(Regex("sell\\s+\\d+\\s+.+")) == true
                            && Material.matchMaterial(event.getLine(1)?.replace(Regex("sell\\s+\\d+\\s+"), "")
                            ?: "") != null))) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["shop-line-1-invalid"])
                return
            }
            if (!(event.getLine(2)?.matches(Regex("for\\s+\\d+\\s+.+")) == true
                            && currencyService.getCurrency(event.getLine(2)
                            ?.replace(Regex("for\\s+\\d+\\s+"), "") ?: "") != null)) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["shop-line-2-invalid"])
                return
            }
            event.setLine(0, "$GREEN[shop]")
            if (!event.getLine(3).equals("admin", ignoreCase = true)) {
                event.setLine(3, character.id.toString())
            } else {
                if (!event.player.hasPermission("rpkit.shops.sign.shop.admin")) {
                    event.block.breakNaturally()
                    event.player.sendMessage(plugin.messages["no-permission-shop-admin"])
                    return
                }
            }
            event.block.getRelative(DOWN).type = CHEST
            val chest = event.block.getRelative(DOWN).state
            if (chest is Chest) {
                val chestData = chest.blockData
                if (chestData is org.bukkit.block.data.type.Chest) {
                    val sign = event.block.state
                    if (sign is Sign) {
                        val signData = sign.blockData
                        if (signData is org.bukkit.block.data.type.Sign) {
                            chestData.facing = signData.rotation
                            chest.update()
                        }
                    }
                }
            }
            if (!event.getLine(3).equals("admin", ignoreCase = true)) {
                shopCountService.setShopCount(character, shopCountService.getShopCount(character) + 1)
            }
        } else if (event.getLine(0) == "[rent]") {
            if (!event.player.hasPermission("rpkit.shops.sign.rent")) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["no-permission-rent"])
                return
            }
            val minecraftProfileService = Services[RPKMinecraftProfileService::class]
            if (minecraftProfileService == null) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["no-minecraft-profile-service"])
                return
            }
            val characterService = Services[RPKCharacterService::class]
            if (characterService == null) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["no-character-service"])
                return
            }
            val currencyService = Services[RPKCurrencyService::class]
            if (currencyService == null) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["no-currency-service"])
                return
            }
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player)
            if (minecraftProfile == null) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["no-minecraft-profile"])
                return
            }
            val character = characterService.getActiveCharacter(minecraftProfile)
            if (character == null) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["no-character"])
                return
            }
            event.setLine(1, character.id.toString())
            if (event.getLine(2)?.matches(Regex("\\d+\\s+.+")) != true) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["rent-line-2-invalid"])
                return
            }
            val currencyName = event.getLine(2)
                    ?.split(Regex("\\s+"))
                    ?.drop(1)
                    ?.joinToString(" ")
                    ?: ""
            if (currencyService.getCurrency(currencyName) == null) {
                event.block.breakNaturally()
                event.player.sendMessage(plugin.messages["rent-line-2-invalid"])
                return
            }
            event.setLine(3, LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
            event.setLine(0, "$GREEN[rent]")
        }
    }

}