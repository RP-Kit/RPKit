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

import com.rpkit.banks.bukkit.bank.RPKBankService
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrencyName
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.shops.bukkit.RPKShopsBukkit
import org.bukkit.ChatColor.GREEN
import org.bukkit.Material
import org.bukkit.Material.AIR
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.BlockFace.UP
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Player interact listener for shops.
 */
class PlayerInteractListener(val plugin: RPKShopsBukkit) : Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action != RIGHT_CLICK_BLOCK) return
        val block = event.clickedBlock
        if (block == null) return
        val state = block.state
        if (state !is Sign) return
        if (state.getLine(0) == "$GREEN[shop]") {
            if (!validateRentSign(block.getRelative(UP))) {
                event.player.sendMessage(plugin.messages["rent-ended"])
                return
            }
            if (state.getLine(1).startsWith("buy")) {
                val chestBlock = block.getRelative(DOWN)
                val chestState = chestBlock.state as? Chest
                if (chestState == null) return
                event.player.openInventory(chestState.blockInventory)
            } else if (state.getLine(1).startsWith("sell")) {
                val amount = state.getLine(1).split(Regex("\\s+"))[1].toInt()
                val materialName = state.getLine(1).split(Regex("\\s+"))[2]
                val material = Material.matchMaterial(materialName)
                        ?: Material.matchMaterial(materialName, true)
                if (material == null) {
                    event.player.sendMessage(plugin.messages["shop-material-invalid"])
                    return
                }
                val price = state.getLine(2).split(Regex("\\s+"))[1].toInt()
                val currencyService = Services[RPKCurrencyService::class.java]
                if (currencyService == null) {
                    event.player.sendMessage(plugin.messages["no-currency-service"])
                    return
                }
                val currencyBuilder = StringBuilder()
                for (i in 2 until state.getLine(2).split(Regex("\\s+")).size) {
                    currencyBuilder.append(state.getLine(2).split(Regex("\\s+"))[i]).append(' ')
                }
                currencyBuilder.deleteCharAt(currencyBuilder.lastIndex)
                val currency = currencyService.getCurrency(RPKCurrencyName(currencyBuilder.toString()))
                if (currency == null) {
                    event.player.sendMessage(plugin.messages["shop-currency-invalid"])
                    return
                }
                val characterService = Services[RPKCharacterService::class.java]
                if (characterService == null) {
                    event.player.sendMessage(plugin.messages["no-character-service"])
                    return
                }
                val isAdminShop = state.getLine(3).equals("admin", ignoreCase = true)
                val ownerCharacter = if (isAdminShop) null else characterService.getCharacter(state.getLine(3).toInt())
                if (ownerCharacter == null && !isAdminShop) {
                    event.player.sendMessage(plugin.messages["shop-character-invalid"])
                    return
                }
                val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                if (minecraftProfileService == null) {
                    event.player.sendMessage(plugin.messages["no-minecraft-profile-service"])
                    return
                }
                val customerMinecraftProfile = minecraftProfileService.getMinecraftProfile(event.player)
                if (customerMinecraftProfile == null) {
                    event.player.sendMessage(plugin.messages["no-minecraft-profile"])
                    return
                }
                val customerCharacter = characterService.getActiveCharacter(customerMinecraftProfile)
                if (customerCharacter == null) {
                    event.player.sendMessage(plugin.messages["no-character"])
                    return
                }
                val item = ItemStack(material)
                val items = ItemStack(material, amount)
                if (!event.player.inventory.containsAtLeast(item, amount)) {
                    event.player.sendMessage(plugin.messages["shop-sell-not-enough-items"])
                    return
                }
                val chestBlock = block.getRelative(DOWN)
                val chestState = chestBlock.state as? Chest
                if (chestState == null) {
                    event.player.sendMessage(plugin.messages["shop-sell-chest-not-found"])
                    return
                }
                val economyService = Services[RPKEconomyService::class.java]
                if (economyService == null) {
                    event.player.sendMessage(plugin.messages["no-economy-service"])
                    return
                }
                val bankService = Services[RPKBankService::class.java]
                if (bankService == null) {
                    event.player.sendMessage(plugin.messages["no-bank-service"])
                    return
                }
                if (isAdminShop) {
                    event.player.inventory.removeItem(items)
                    chestState.blockInventory.addItem(items)
                    economyService.setBalance(customerCharacter, currency, economyService.getBalance(customerCharacter, currency) + price)
                } else if (ownerCharacter != null) {
                    if (bankService.getBalance(ownerCharacter, currency) < price) {
                        event.player.sendMessage(plugin.messages["shop-sell-not-enough-money"])
                        return
                    }
                    event.player.inventory.removeItem(items)
                    chestState.blockInventory.addItem(items)
                    bankService.setBalance(ownerCharacter, currency, bankService.getBalance(ownerCharacter, currency) - price)
                    economyService.setBalance(customerCharacter, currency, economyService.getBalance(customerCharacter, currency) + price)
                } else {
                    event.player.sendMessage(plugin.messages["shop-character-invalid"])
                }
            }
        } else if (state.getLine(0) == "$GREEN[rent]") {
            val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
            if (minecraftProfileService == null) {
                event.player.sendMessage(plugin.messages["no-minecraft-profile-service"])
                return
            }
            val characterService = Services[RPKCharacterService::class.java]
            if (characterService == null) {
                event.player.sendMessage(plugin.messages["no-character-service"])
                return
            }
            val economyService = Services[RPKEconomyService::class.java]
            if (economyService == null) {
                event.player.sendMessage(plugin.messages["no-economy-service"])
                return
            }
            val currencyService = Services[RPKCurrencyService::class.java]
            if (currencyService == null) {
                event.player.sendMessage(plugin.messages["no-currency-service"])
                return
            }
            val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player)
            if (minecraftProfile == null) {
                event.player.sendMessage(plugin.messages["no-minecraft-profile"])
                return
            }
            val character = characterService.getActiveCharacter(minecraftProfile)
            val rentCharacter = characterService.getCharacter(state.getLine(1).toInt())
            val cost = state.getLine(2).split(Regex("\\s+"))[0].toInt()
            val currency = currencyService.getCurrency(RPKCurrencyName(state.getLine(2)
                    .split(Regex("\\s+"))
                    .drop(1)
                    .joinToString(" ")))
            if (character == null) {
                event.player.sendMessage(plugin.messages["no-character"])
                return
            }
            val shopSign = block.getRelative(DOWN).state as? Sign
            if (shopSign == null) {
                event.player.sendMessage(plugin.messages["rent-no-shop"])
                return
            }
            val isAdminShop = shopSign.getLine(3).equals("admin", ignoreCase = true)
            val shopCharacter = if (isAdminShop) null else characterService.getCharacter(shopSign.getLine(3).toInt())
            if (shopCharacter == null || shopCharacter.id == character.id) {
                if (rentCharacter != null) {
                    if (currency != null) {
                        economyService.transfer(character, rentCharacter, currency, cost)
                        state.setLine(3, LocalDate.parse(state.getLine(3), DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                                .plusDays(1L).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                        state.update()
                        event.player.sendMessage(plugin.messages["rent-paid"])
                    } else {
                        event.player.sendMessage(plugin.messages["rent-invalid-currency"])
                    }
                } else {
                    event.player.sendMessage(plugin.messages["rent-invalid-character"])
                    block.type = AIR
                }
            } else {
                event.player.sendMessage(plugin.messages["rent-not-owner"])
            }
        }
    }

    private fun validateRentSign(block: Block): Boolean {
        val sign = block.state as? Sign ?: return true
        if (sign.getLine(0) != "$GREEN[rent]") return true
        return LocalDate.now().isBefore(LocalDate.parse(sign.getLine(3), DateTimeFormatter.ofPattern("yyyy-MM-dd")))
    }

}