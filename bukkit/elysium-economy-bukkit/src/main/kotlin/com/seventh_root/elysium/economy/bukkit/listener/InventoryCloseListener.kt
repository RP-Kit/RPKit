package com.seventh_root.elysium.economy.bukkit.listener

import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider
import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.currency.BukkitCurrencyProvider
import com.seventh_root.elysium.economy.bukkit.economy.BukkitEconomyProvider
import com.seventh_root.elysium.players.bukkit.player.BukkitPlayerProvider
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

class InventoryCloseListener(private val plugin: ElysiumEconomyBukkit) : Listener {

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.inventory.title.toLowerCase().contains("wallet")) {
            val bukkitPlayer = event.player
            if (bukkitPlayer is Player) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(BukkitPlayerProvider::class.java)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(BukkitCharacterProvider::class.java)
                val currencyProvider = plugin.core.serviceManager.getServiceProvider(BukkitCurrencyProvider::class.java)
                val economyProvider = plugin.core.serviceManager.getServiceProvider(BukkitEconomyProvider::class.java)
                val currency = currencyProvider.getCurrency(event.inventory.title.substringAfterLast("[").substringBeforeLast("]"))
                if (currency != null) {
                    val amount = event.inventory.contents
                            .filter { item ->
                                item != null
                                        && item.type === currency.material
                                        && item.hasItemMeta()
                                        && item.itemMeta.hasDisplayName()
                                        && item.itemMeta.displayName.equals(currency.nameSingular)
                            }
                            .map { item -> item.amount }
                            .sum()
                    val player = playerProvider.getPlayer(bukkitPlayer)
                    val character = characterProvider.getActiveCharacter(player)
                    if (character != null) {
                        economyProvider.setBalance(character, currency, amount)
                    }
                }
            }
        }
    }

}
