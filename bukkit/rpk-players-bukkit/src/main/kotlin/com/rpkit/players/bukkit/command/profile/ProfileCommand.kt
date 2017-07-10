package com.rpkit.players.bukkit.command.profile

import com.rpkit.players.bukkit.RPKPlayersBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class ProfileCommand(private val plugin: RPKPlayersBukkit): CommandExecutor {

    val profileCreateCommand = ProfileCreateCommand(plugin)
    val profileLoginCommand = ProfileLoginCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            val newArgs = args.drop(1).toTypedArray()
            if (args[0].equals("create", ignoreCase = true) || args[0].equals("new", ignoreCase = true)) {
                return profileCreateCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("login", ignoreCase = true) || args[0].equals("link", ignoreCase = true)) {
                return profileLoginCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(plugin.messages["profile-usage"])
            }
        } else {
            sender.sendMessage(plugin.messages["profile-usage"])
        }
        return true
    }
}