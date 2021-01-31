package com.rpkit.permissions.bukkit.command.charactergroup

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.group.RPKGroupService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CharacterGroupListCommand(private val plugin: RPKPermissionsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!sender.hasPermission("rpkit.permissions.command.character.group.list")) {
            sender.sendMessage(plugin.messages["no-permission-character-group-list"])
            return true
        }

        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["no-character"])
            return true
        }

        val rpkGroupService = Services[RPKGroupService::class.java]
        if (rpkGroupService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }

        val rpkCharacterService = Services[RPKCharacterService::class.java]
        if (rpkCharacterService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }

        val characters = rpkCharacterService.getCharacters(args[0]);
        if (characters.isEmpty()) {
            sender.sendMessage(plugin.messages["no-character"])
            return true
        }

        characters
            .filter { it.name.equals(args[0], ignoreCase = true) }
            .forEach {
                sender.sendMessage(plugin.messages["character-group-list-title"])
                for (group in rpkGroupService.getGroups(it)) {
                    sender.sendMessage(
                        plugin.messages["group-list-item", mapOf(
                            "group" to group.name.value
                        )]
                    )
                }
            }
        return true
    }

}
