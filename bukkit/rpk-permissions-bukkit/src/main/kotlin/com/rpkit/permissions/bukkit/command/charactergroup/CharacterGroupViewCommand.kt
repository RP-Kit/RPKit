package com.rpkit.permissions.bukkit.command.charactergroup

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.bukkit.extension.levenshtein
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.group.RPKGroupService
import com.rpkit.players.bukkit.profile.RPKProfileDiscriminator
import com.rpkit.players.bukkit.profile.RPKProfileName
import com.rpkit.players.bukkit.profile.RPKProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CharacterGroupViewCommand(private val plugin: RPKPermissionsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return true
        }
        if (!sender.hasPermission("rpkit.permissions.command.character.group.view")) {
            sender.sendMessage(plugin.messages.noPermissionCharacterGroupView)
            return true
        }

        if (args.size < 2) {
            sender.sendMessage(plugin.messages.characterGroupViewUsage)
            return true
        }

        val groupService = Services[RPKGroupService::class.java]
        if (groupService == null) {
            sender.sendMessage(plugin.messages.noGroupService)
            return true
        }

        val profileService = Services[RPKProfileService::class.java]
        if (profileService == null) {
            sender.sendMessage(plugin.messages.noProfileService)
            return true
        }

        if (!args[0].contains("#")) {
            sender.sendMessage(plugin.messages.characterGroupViewInvalidProfileName)
            return true
        }
        val nameParts = args[0].split("#")
        val name = nameParts[0]
        val discriminator = nameParts[1].toIntOrNull()
        if (discriminator == null) {
            sender.sendMessage(plugin.messages.characterGroupViewInvalidProfileName)
            return true
        }
        val profile = profileService.getProfile(RPKProfileName(name), RPKProfileDiscriminator(discriminator))
        if (profile == null) {
            sender.sendMessage(plugin.messages.characterGroupViewInvalidProfile)
            return true
        }

        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages.noCharacterService)
            return true
        }

        val characters = characterService.getCharacters(profile)
        if (characters.isEmpty()) {
            sender.sendMessage(plugin.messages.noCharacter)
            return true
        }

        characters.minByOrNull { args[0].levenshtein(it.name) }
            ?.let { character ->
                sender.sendMessage(plugin.messages.characterGroupViewTitle.withParameters(
                    character = character
                ))
                for (group in groupService.getGroups(character)) {
                    sender.sendMessage(
                        plugin.messages.characterGroupViewItem.withParameters(
                            group = group
                        )
                    )
                }
            }
        return true
    }

}
