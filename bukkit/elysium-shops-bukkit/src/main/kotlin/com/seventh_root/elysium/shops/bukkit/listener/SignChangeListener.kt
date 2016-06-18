package com.seventh_root.elysium.shops.bukkit.listener

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import com.seventh_root.elysium.shops.bukkit.ElysiumShopsBukkit
import com.seventh_root.elysium.shops.bukkit.shopcount.ElysiumShopCountProvider
import org.bukkit.ChatColor
import org.bukkit.ChatColor.GREEN
import org.bukkit.Material
import org.bukkit.Material.CHEST
import org.bukkit.block.BlockFace.DOWN
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.SignChangeEvent


class SignChangeListener(private val plugin: ElysiumShopsBukkit): Listener {

    @EventHandler
    fun onSignChange(event: SignChangeEvent) {
        if (event.getLine(0) == "[shop]") {
            if (event.player.hasPermission("elysium.shops.sign.shop")) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class.java)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class.java)
                val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class.java)
                val shopCountProvider = plugin.core.serviceManager.getServiceProvider(ElysiumShopCountProvider::class.java)
                val player = playerProvider.getPlayer(event.player)
                val character = characterProvider.getActiveCharacter(player)
                if (character != null) {
                    val shopCount = shopCountProvider.getShopCount(character)
                    if (shopCount < plugin.config.getInt("shops.limit") || event.player.hasPermission("elysium.shops.sign.shop.nolimit")) {
                        if (!(event.getLine(1).matches(Regex("buy\\s+\\d+"))
                                || (event.getLine(1).matches(Regex("sell\\s+\\d+\\s+.+"))
                                && Material.matchMaterial(event.getLine(1).replace(Regex("sell\\s+\\d+\\s+"), "")) != null))) {
                            event.block.breakNaturally()
                            event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.shop-line-1-invalid")))
                            return
                        }
                        if (!(event.getLine(2).matches(Regex("for\\s+\\d+\\s+.+")) && currencyProvider.getCurrency(event.getLine(2).replace(Regex("for\\s+\\d+\\s+"), "")) != null)) {
                            event.block.breakNaturally()
                            event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.shop-line-2-invalid")))
                            return
                        }
                        event.setLine(0, GREEN.toString() + "[shop]")
                        event.setLine(3, character.id.toString())
                        event.block.getRelative(DOWN).type = CHEST
                        shopCountProvider.setShopCount(character, shopCountProvider.getShopCount(character) + 1)
                    } else {
                        event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-shop-limit")))
                        event.block.breakNaturally()
                    }
                } else {
                    event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                }
            } else {
                event.player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-shop")))
                event.block.breakNaturally()
            }
        }
    }

}