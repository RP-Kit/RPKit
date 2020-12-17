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

import com.rpkit.banks.bukkit.bank.RPKBankService
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.economy.bukkit.currency.RPKCurrencyService
import com.rpkit.economy.bukkit.economy.RPKEconomyService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.shops.bukkit.RPKShopsBukkit
import org.bukkit.ChatColor.GREEN
import org.bukkit.block.Block
import org.bukkit.block.BlockFace.UP
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Inventory click listener for shops.
 */
class InventoryClickListener(val plugin: RPKShopsBukkit) : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val chest = event.inventory.holder
        if (chest !is Chest) return
        val sign = chest.block.getRelative(UP).state
        if (sign !is Sign) return
        if (sign.getLine(0) != "$GREEN[shop]") return
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            event.whoClicked.sendMessage(plugin.messages["no-minecraft-profile-service"])
            event.isCancelled = true
            return
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            event.whoClicked.sendMessage(plugin.messages["no-character-service"])
            event.isCancelled = true
            return
        }
        val economyService = Services[RPKEconomyService::class.java]
        if (economyService == null) {
            event.whoClicked.sendMessage(plugin.messages["no-economy-service"])
            event.isCancelled = true
            return
        }
        val bankService = Services[RPKBankService::class.java]
        if (bankService == null) {
            event.whoClicked.sendMessage(plugin.messages["no-bank-service"])
            event.isCancelled = true
            return
        }
        val currencyService = Services[RPKCurrencyService::class.java]
        if (currencyService == null) {
            event.whoClicked.sendMessage(plugin.messages["no-currency-service"])
            event.isCancelled = true
            return
        }
        val sellerCharacter = if (sign.getLine(3).equals("admin", ignoreCase = true)) {
            null
        } else {
            characterService.getCharacter(sign.getLine(3).toInt()) ?: return
        }
        val buyerBukkitPlayer = event.whoClicked as? Player ?: return
        val buyerMinecraftProfile = minecraftProfileService.getMinecraftProfile(buyerBukkitPlayer)
        if (buyerMinecraftProfile == null) {
            event.whoClicked.sendMessage(plugin.messages["no-minecraft-profile"])
            return
        }
        if (!validateRentSign(chest.block.getRelative(UP, 2))) {
            buyerBukkitPlayer.sendMessage(plugin.messages["rent-ended"])
            return
        }
        val buyerCharacter = characterService.getActiveCharacter(buyerMinecraftProfile)
        if (buyerCharacter == null) {
            buyerBukkitPlayer.sendMessage(plugin.messages["no-character"])
            return
        }
        if (buyerCharacter == sellerCharacter) {
            return
        }
        event.isCancelled = true
        if (sign.getLine(1).startsWith("buy")) {
            val amount = sign.getLine(1).split(Regex("\\s+"))[1].toInt()
            val price = sign.getLine(2).split(Regex("\\s+"))[1].toInt()
            val currencyWords = sign.getLine(2).split(Regex("\\s+"))
            val currency = currencyService.getCurrency(currencyWords.subList(2, currencyWords.size).joinToString(" "))
                    ?: return
            val item = event.currentItem ?: return
            val amtItem = ItemStack(item)
            amtItem.amount = amount
            if (!chest.blockInventory.containsAtLeast(item, amount)) {
                buyerBukkitPlayer.sendMessage(plugin.messages["not-enough-shop-items"])
                return
            }
            if (economyService.getBalance(buyerCharacter, currency) < price) {
                buyerBukkitPlayer.sendMessage(plugin.messages["not-enough-money"])
                return
            }
            economyService.setBalance(buyerCharacter, currency, economyService.getBalance(buyerCharacter, currency) - price)
            if (sellerCharacter != null) {
                bankService.setBalance(sellerCharacter, currency, bankService.getBalance(sellerCharacter, currency) + price)
            }
            buyerBukkitPlayer.inventory.addItem(amtItem)
            chest.blockInventory.removeItem(amtItem)
        } else if (sign.getLine(1).startsWith("sell")) {
            event.whoClicked.sendMessage(plugin.messages["no-stealing"])
            event.isCancelled = true
        }
    }

    private fun validateRentSign(block: Block): Boolean {
        val sign = block.state as? Sign ?: return true
        if (sign.getLine(0) != "$GREEN[rent]") return true
        return LocalDate.now().isBefore(LocalDate.parse(sign.getLine(3), DateTimeFormatter.ofPattern("yyyy-MM-dd")))
    }

}