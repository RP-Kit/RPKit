package com.rpkit.languages.bukkit.command

import com.rpkit.languages.bukkit.RPKLanguagesBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class LanguageCommand(private val plugin: RPKLanguagesBukkit) : CommandExecutor {

    private val languageListCommand = LanguageListCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            val newArgs = args.drop(1).toTypedArray()
            when (args[0].uppercase()) {
                "LIST" -> languageListCommand.onCommand(sender, command, label, newArgs)
                else -> sender.sendMessage(plugin.messages.languageUsage)
            }
        } else {
            sender.sendMessage(plugin.messages.languageUsage)
        }
        return true
    }
}
