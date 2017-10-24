package com.rpkit.players.bukkit.command.profile

import com.rpkit.players.bukkit.RPKPlayersBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class ProfileCommand(private val plugin: RPKPlayersBukkit): CommandExecutor {

    val profileNameCommand = ProfileNameCommand(plugin)
    val profilePasswordCommand = ProfilePasswordCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["profile-usage"])
            return true
        }
        val newArgs = args.drop(1).toTypedArray()
        return when (args[0].toLowerCase()) {
            "name" -> profileNameCommand.onCommand(sender, command, label, newArgs)
            "password" -> profilePasswordCommand.onCommand(sender, command, label, newArgs)
            else -> {
                sender.sendMessage(plugin.messages["profile-usage"])
                true
            }
        }
    }
}