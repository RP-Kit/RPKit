package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.logmessage.RPKLogMessageProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ToggleLogMessagesCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.togglelogmessages")) {
            if (sender is Player) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                val logMessageProvider = plugin.core.serviceManager.getServiceProvider(RPKLogMessageProvider::class)
                val player = playerProvider.getPlayer(sender)
                logMessageProvider.setLogMessagesEnabled(player, !logMessageProvider.isLogMessagesEnabled(player))
                sender.sendMessage(plugin.messages["toggle-log-messages-valid", mapOf(
                        Pair("enabled", if (logMessageProvider.isLogMessagesEnabled(player)) "enabled" else "disabled")
                )])
            } else {
                sender.sendMessage(plugin.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-toggle-log-messages"])
        }
        return true
    }
}
