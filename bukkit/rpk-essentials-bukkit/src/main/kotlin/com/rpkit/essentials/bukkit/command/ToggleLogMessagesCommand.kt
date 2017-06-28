package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.logmessage.RPKLogMessageProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ToggleLogMessagesCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.togglelogmessages")) {
            if (sender is Player) {
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val logMessageProvider = plugin.core.serviceManager.getServiceProvider(RPKLogMessageProvider::class)
                val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
                if (minecraftProfile != null) {
                    logMessageProvider.setLogMessagesEnabled(minecraftProfile, !logMessageProvider.isLogMessagesEnabled(minecraftProfile))
                    sender.sendMessage(plugin.messages["toggle-log-messages-valid", mapOf(
                            Pair("enabled", if (logMessageProvider.isLogMessagesEnabled(minecraftProfile)) "enabled" else "disabled")
                    )])
                } else {
                    sender.sendMessage(plugin.messages["no-minecraft-profile"])
                }
            } else {
                sender.sendMessage(plugin.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-toggle-log-messages"])
        }
        return true
    }
}
