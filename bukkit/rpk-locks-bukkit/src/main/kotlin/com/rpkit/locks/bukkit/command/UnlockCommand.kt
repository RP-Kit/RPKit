package com.rpkit.locks.bukkit.command

import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.lock.RPKLockProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class UnlockCommand(private val plugin: RPKLocksBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.locks.command.unlock")) {
            if (sender is Player) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                val lockProvider = plugin.core.serviceManager.getServiceProvider(RPKLockProvider::class)
                val player = playerProvider.getPlayer(sender)
                lockProvider.setUnclaiming(player, true)
                sender.sendMessage(plugin.core.messages["unlock-valid"])
            } else {
                sender.sendMessage(plugin.core.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.core.messages["no-permission-unlock"])
        }
        return true
    }
}