package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SetSpawnCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.setspawn")) {
            if (sender is Player) {
                sender.world.setSpawnLocation(sender.location.blockX, sender.location.blockY, sender.location.blockZ)
                sender.sendMessage(plugin.messages["set-spawn-valid", mapOf(
                        Pair("world", sender.world.name)
                )])
            } else {
                sender.sendMessage(plugin.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-set-spawn"])
        }
        return true
    }

}
