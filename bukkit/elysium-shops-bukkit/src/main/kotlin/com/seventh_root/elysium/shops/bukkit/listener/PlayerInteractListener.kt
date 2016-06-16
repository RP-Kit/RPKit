package com.seventh_root.elysium.shops.bukkit.listener

import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider
import com.seventh_root.elysium.economy.bukkit.currency.BukkitCurrencyProvider
import com.seventh_root.elysium.economy.bukkit.economy.BukkitEconomyProvider
import com.seventh_root.elysium.players.bukkit.player.BukkitPlayerProvider
import com.seventh_root.elysium.shops.bukkit.ElysiumShopsBukkit
import org.bukkit.ChatColor.GREEN
import org.bukkit.Material
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack


class PlayerInteractListener(val plugin: ElysiumShopsBukkit): Listener {

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.action == RIGHT_CLICK_BLOCK) {
            val block = event.clickedBlock
            val state = block.state
            if (state is Sign) {
                if (state.getLine(0) == GREEN.toString() + "[shop]") {
                    if (state.getLine(1).startsWith("buy")) {
                        val chestBlock = block.getRelative(DOWN)
                        val chestState = chestBlock.state as? Chest
                        if (chestState != null) {
                            event.player.openInventory(chestState.blockInventory)
                        }
                    } else if (state.getLine(1).startsWith("sell")) {
                        val amount = state.getLine(1).split(Regex("\\s+"))[1].toInt()
                        val material = Material.matchMaterial(state.getLine(1).split(Regex("\\s+"))[2])
                        val price = state.getLine(2).split(Regex("\\s+"))[1].toInt()
                        val currencyProvider = plugin.core.serviceManager.getServiceProvider(BukkitCurrencyProvider::class.java)
                        val currencyBuilder = StringBuilder()
                        for (i in 2..state.getLine(2).split(Regex("\\s+")).size - 1) {
                            currencyBuilder.append(state.getLine(2).split(Regex("\\s+"))[i]).append(' ')
                        }
                        currencyBuilder.deleteCharAt(currencyBuilder.lastIndex)
                        val currency = currencyProvider.getCurrency(currencyBuilder.toString())
                        if (currency != null) {
                            val characterProvider = plugin.core.serviceManager.getServiceProvider(BukkitCharacterProvider::class.java)
                            val ownerCharacter = characterProvider.getCharacter(state.getLine(3).toInt())
                            if (ownerCharacter != null) {
                                val playerProvider = plugin.core.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
                                val customerPlayer = playerProvider.getPlayer(event.player)
                                val customerCharacter = characterProvider.getActiveCharacter(customerPlayer)
                                if (customerCharacter != null) {
                                    val item = ItemStack(material)
                                    val items = ItemStack(material, amount)
                                    if (event.player.inventory.containsAtLeast(item, amount)) {
                                        val chestBlock = block.getRelative(DOWN)
                                        val chestState = chestBlock.state as? Chest
                                        if (chestState != null) {
                                            val economyProvider = plugin.core.serviceManager.getServiceProvider(BukkitEconomyProvider::class.java)
                                            if (economyProvider.getBalance(ownerCharacter, currency) >= price) {
                                                event.player.inventory.removeItem(items)
                                                chestState.blockInventory.addItem(items)
                                                economyProvider.transfer(ownerCharacter, customerCharacter, currency, price)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}