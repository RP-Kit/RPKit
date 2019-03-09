package com.rpkit.classes.bukkit.command.`class`

import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.classes.bukkit.classes.RPKClassProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class ClassListCommand(private val plugin: RPKClassesBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.classes.command.class.list")) {
            sender.sendMessage(plugin.messages["no-permission-class-list"])
            return true
        }
        val classProvider = plugin.core.serviceManager.getServiceProvider(RPKClassProvider::class)
        sender.sendMessage(plugin.messages["class-list-title"])
        for (`class` in classProvider.classes) {
            sender.sendMessage(plugin.messages["class-list-item", mapOf(Pair("class", `class`.name))])
        }
        return true
    }

}