package com.seventh_root.elysium.shops.bukkit.listener

import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider
import com.seventh_root.elysium.economy.bukkit.currency.BukkitCurrencyProvider
import com.seventh_root.elysium.economy.bukkit.economy.BukkitEconomyProvider
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider
import com.seventh_root.elysium.shops.bukkit.ElysiumShopsBukkit
import org.bukkit.ChatColor
import org.bukkit.ChatColor.GREEN
import org.bukkit.block.BlockFace.UP
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack


class InventoryClickListener(val plugin: ElysiumShopsBukkit): Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val chest = event.inventory.holder
        if (chest is Chest) {
            val sign = chest.block.getRelative(UP).state
            if (sign is Sign) {
                if (sign.getLine(0) == GREEN.toString() + "[shop]") {
                    val playerProvider = plugin.core.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
                    val characterProvider = plugin.core.serviceManager.getServiceProvider(BukkitCharacterProvider::class.java)
                    val economyProvider = plugin.core.serviceManager.getServiceProvider(BukkitEconomyProvider::class.java)
                    val currencyProvider = plugin.core.serviceManager.getServiceProvider(BukkitCurrencyProvider::class.java)
                    val sellerCharacter = characterProvider.getCharacter(Integer.parseInt(sign.getLine(3))) ?: return
                    val buyerBukkitPlayer = event.whoClicked as? Player ?: return
                    val buyerPlayer = playerProvider.getPlayer(buyerBukkitPlayer)
                    val buyerCharacter = characterProvider.getActiveCharacter(buyerPlayer)
                    if (buyerCharacter == null) {
                        buyerBukkitPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                        return
                    }
                    if (buyerCharacter == sellerCharacter) {
                        return
                    }
                    event.isCancelled = true
                    if (sign.getLine(1).startsWith("buy")) {
                        val amount = Integer.parseInt(sign.getLine(1).split(Regex("\\s+"))[1])
                        val price = Integer.parseInt(sign.getLine(2).split(Regex("\\s+"))[1])
                        val currencyBuilder = StringBuilder()
                        for (i in 2..sign.getLine(2).split(Regex("\\s+")).size - 1) {
                            currencyBuilder.append(sign.getLine(2).split(Regex("\\s+"))[i])
                        }
                        val currency = currencyProvider.getCurrency(currencyBuilder.toString()) ?: return
                        val item = event.currentItem ?: return
                        val amtItem = ItemStack(item)
                        amtItem.amount = amount
                        if (chest.blockInventory.containsAtLeast(item, amount)) {
                            if (economyProvider.getBalance(buyerCharacter, currency) >= price) {
                                economyProvider.transfer(buyerCharacter, sellerCharacter, currency, price)
                                buyerBukkitPlayer.inventory.addItem(amtItem)
                                chest.blockInventory.removeItem(amtItem)
                            } else {
                                buyerBukkitPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-enough-money")))
                            }
                        } else {
                            buyerBukkitPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-enough-shop-items")))
                        }
                    } else if (sign.getLine(1).startsWith("sell")) {
                        event.whoClicked.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-stealing")))
                    }
                }
            }
        }
    }

}