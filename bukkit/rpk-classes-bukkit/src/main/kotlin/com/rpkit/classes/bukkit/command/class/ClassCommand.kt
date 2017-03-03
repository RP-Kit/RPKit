package com.rpkit.classes.bukkit.command.`class`

import com.rpkit.classes.bukkit.RPKClassesBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class ClassCommand(private val plugin: RPKClassesBukkit): CommandExecutor {

    private val classSetCommand = ClassSetCommand(plugin)
    private val classListCommand = ClassListCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            val newArgs = args.drop(1).toTypedArray()
            if (args[0].equals("set", ignoreCase = true)) {
                return classSetCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("list", ignoreCase = true)) {
                return classListCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(plugin.messages["class-usage"])
            }
        } else {
            sender.sendMessage(plugin.messages["class-usage"])
        }
        return true
    }

}