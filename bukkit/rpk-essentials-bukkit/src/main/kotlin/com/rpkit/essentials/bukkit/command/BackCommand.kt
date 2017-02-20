package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.locationhistory.bukkit.locationhistory.RPKLocationHistoryProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class BackCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("rpkit.essentials.command.back")) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                val locationHistoryProvider = plugin.core.serviceManager.getServiceProvider(RPKLocationHistoryProvider::class)
                val player = playerProvider.getPlayer(sender)
                val previousLocation = locationHistoryProvider.getPreviousLocation(player)
                if (previousLocation != null) {
                    sender.teleport(previousLocation)
                    sender.sendMessage(plugin.messages["back-valid"])
                } else {
                    sender.sendMessage(plugin.messages["back-invalid-no-locations"])
                }
            } else {
                sender.sendMessage(plugin.messages["no-permission-back"])
            }
        } else {
            sender.sendMessage(plugin.messages["not-from-console"])
        }
        return true
    }
}
