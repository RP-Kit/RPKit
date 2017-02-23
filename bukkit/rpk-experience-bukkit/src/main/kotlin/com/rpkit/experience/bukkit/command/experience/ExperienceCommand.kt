package com.rpkit.experience.bukkit.command.experience

import com.rpkit.experience.bukkit.RPKExperienceBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class ExperienceCommand(private val plugin: RPKExperienceBukkit): CommandExecutor {

    private val experienceAddCommand = ExperienceAddCommand(plugin)
    private val experienceSetCommand = ExperienceSetCommand(plugin)
    private val experienceSetLevelCommand = ExperienceSetLevelCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            val newArgs = args.drop(1).toTypedArray()
            if (args[0].equals("add", ignoreCase = true)) {
                return experienceAddCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("set", ignoreCase = true)) {
                return experienceSetCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("setlevel", ignoreCase = true)) {
                return experienceSetLevelCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(plugin.messages["experience-usage"])
            }
        } else {
            sender.sendMessage(plugin.messages["experience-usage"])
        }
        return true
    }
}