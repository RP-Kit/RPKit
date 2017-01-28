package com.rpkit.locks.bukkit.command

import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.lock.RPKLockProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class GetKeyCommand(private val plugin: RPKLocksBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.locks.command.getkey")) {
            if (sender is Player) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                val lockProvider = plugin.core.serviceManager.getServiceProvider(RPKLockProvider::class)
                lockProvider.setGettingKey(playerProvider.getPlayer(sender), true)
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.get-key-valid")))
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-get-key")))
        }
        return true
    }
}