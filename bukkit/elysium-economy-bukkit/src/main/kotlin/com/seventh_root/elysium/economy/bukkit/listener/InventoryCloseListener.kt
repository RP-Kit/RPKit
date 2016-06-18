package com.seventh_root.elysium.economy.bukkit.listener

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyProvider
import com.seventh_root.elysium.economy.bukkit.economy.ElysiumEconomyProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent

class InventoryCloseListener(private val plugin: ElysiumEconomyBukkit): Listener {

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.inventory.title.toLowerCase().contains("wallet")) {
            val bukkitPlayer = event.player
            if (bukkitPlayer is Player) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class.java)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class.java)
                val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class.java)
                val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class.java)
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
