package com.seventh_root.elysium.economy.bukkit.command.money

import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacter
import com.seventh_root.elysium.characters.bukkit.character.ElysiumCharacterProvider
import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrencyProvider
import com.seventh_root.elysium.economy.bukkit.economy.ElysiumEconomyProvider
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.Conversable
import org.bukkit.entity.Player


class MoneyViewCommand(private val plugin: ElysiumEconomyBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender is Conversable) {
            val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class.java)
            val characterProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCharacterProvider::class.java)
            val economyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumEconomyProvider::class.java)
            val currencyProvider = plugin.core.serviceManager.getServiceProvider(ElysiumCurrencyProvider::class.java)
            val bukkitPlayer = if (args.size > 0) plugin.server.getPlayer(args[0]) else if (sender is Player) sender else null
            if (bukkitPlayer != null) {
                val player = playerProvider.getPlayer(bukkitPlayer)
                var character: ElysiumCharacter?
                if (args.size > 1) {
                    val nameBuilder = StringBuilder()
                    for (i in 1..args.size - 2) {
                        nameBuilder.append(args[i]).append(' ')
                    }
                    nameBuilder.append(args[args.size - 1])
                    val name = nameBuilder.toString()
                    character = characterProvider.getCharacters(player)
                            .filter { character -> character.name.equals(name) }
                            .firstOrNull()
                } else {
                    character = characterProvider.getActiveCharacter(player)
                }
                val finalCharacter = character
                if (finalCharacter != null) {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-view-valid")))
                    sender.sendMessage(currencyProvider.currencies
                            .map { currency ->
                                ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-view-valid-list-item"))
                                        .replace("\$currency", currency.name)
                                        .replace("\$balance", economyProvider.getBalance(finalCharacter, currency).toString())
                            }
                            .toTypedArray()
                    )
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
            }
        }
        return true
    }

}
