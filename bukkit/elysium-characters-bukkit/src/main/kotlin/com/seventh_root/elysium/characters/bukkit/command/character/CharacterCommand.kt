package com.seventh_root.elysium.characters.bukkit.command.character

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class CharacterCommand(private val plugin: ElysiumCharactersBukkit): CommandExecutor {

    private val characterSetCommand: CharacterSetCommand
    private val characterCardCommand: CharacterCardCommand
    private val characterSwitchCommand: CharacterSwitchCommand
    private val characterListCommand: CharacterListCommand
    private val characterNewCommand: CharacterNewCommand
    private val characterDeleteCommand: CharacterDeleteCommand

    init {
        characterSetCommand = CharacterSetCommand(plugin)
        characterCardCommand = CharacterCardCommand(plugin)
        characterSwitchCommand = CharacterSwitchCommand(plugin)
        characterListCommand = CharacterListCommand(plugin)
        characterNewCommand = CharacterNewCommand(plugin)
        characterDeleteCommand = CharacterDeleteCommand(plugin)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.size > 0) {
            var newArgsList: MutableList<String> = arrayListOf()
            for (i: Int in 1..(args.size - 1)) {
                newArgsList.add(args[i])
            }
            var newArgs = newArgsList.toTypedArray()
            if (args[0].equals("set", ignoreCase = true)) {
                return characterSetCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("card", ignoreCase = true) || args[0].equals("show", ignoreCase = true) || args[0].equals("view", ignoreCase = true)) {
                return characterCardCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("switch", ignoreCase = true)) {
                return characterSwitchCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("list", ignoreCase = true)) {
                return characterListCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("new", ignoreCase = true) || args[0].equals("create", ignoreCase = true)) {
                return characterNewCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("delete", ignoreCase = true)) {
                return characterDeleteCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-usage")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.character-usage")))
        }
        return true
    }

}
