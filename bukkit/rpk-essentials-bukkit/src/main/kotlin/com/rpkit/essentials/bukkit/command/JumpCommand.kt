package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class JumpCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.jump")) {
            if (sender is Player) {
                val transparent: Set<Material>? = null
                val block = sender.getTargetBlock(transparent, 64)
                if (block != null) {
                    sender.teleport(block.location)
                    sender.sendMessage(plugin.core.messages["jump-valid"])
                } else {
                    sender.sendMessage(plugin.core.messages["jump-invalid-block"])
                }
            } else {
                sender.sendMessage(plugin.core.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.core.messages["no-permission-jump"])
        }
        return true
    }

}
