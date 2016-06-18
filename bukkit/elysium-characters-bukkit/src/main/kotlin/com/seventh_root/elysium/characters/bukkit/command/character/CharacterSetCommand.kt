package com.seventh_root.elysium.characters.bukkit.command.character

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class CharacterSetCommand(private val plugin: ElysiumCharactersBukkit): CommandExecutor {

    private val characterSetPlayerCommand: CharacterSetPlayerCommand
    private val characterSetNameCommand: CharacterSetNameCommand
    private val characterSetGenderCommand: CharacterSetGenderCommand
    private val characterSetAgeCommand: CharacterSetAgeCommand
    private val characterSetRaceCommand: CharacterSetRaceCommand
    private val characterSetDescriptionCommand: CharacterSetDescriptionCommand
    private val characterSetDeadCommand: CharacterSetDeadCommand

    init {
        characterSetPlayerCommand = CharacterSetPlayerCommand(plugin)
        characterSetNameCommand = CharacterSetNameCommand(plugin)
        characterSetGenderCommand = CharacterSetGenderCommand(plugin)
        characterSetAgeCommand = CharacterSetAgeCommand(plugin)
        characterSetRaceCommand = CharacterSetRaceCommand(plugin)
        characterSetDescriptionCommand = CharacterSetDescriptionCommand(plugin)
        characterSetDeadCommand = CharacterSetDeadCommand(plugin)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.size > 0) {
            var newArgsList: MutableList<String> = arrayListOf()
            for (i: Int in 1..(args.size - 1)) {
                newArgsList.add(args[i])
            }
            var newArgs = newArgsList.toTypedArray()
            if (args[0].equals("player", ignoreCase = true)) {
                return characterSetPlayerCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("name", ignoreCase = true)) {
                return characterSetNameCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("gender", ignoreCase = true)) {
                return characterSetGenderCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("age", ignoreCase = true)) {
                return characterSetAgeCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("race", ignoreCase = true)) {
                return characterSetRaceCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("description", ignoreCase = true) || args[0].equals("desc", ignoreCase = true)) {
                return characterSetDescriptionCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("dead", ignoreCase = true)) {
                return characterSetDeadCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-usage")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-set-usage")))
        }
        return true
    }
}
