package com.rpkit.permissions.bukkit.command.charactergroup

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.bukkit.extension.levenshtein
import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.*
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.group.RPKGroup
import com.rpkit.permissions.bukkit.group.RPKGroupName
import com.rpkit.permissions.bukkit.group.RPKGroupService
import com.rpkit.players.bukkit.command.result.InvalidTargetProfileFailure
import com.rpkit.players.bukkit.profile.RPKProfileDiscriminator
import com.rpkit.players.bukkit.profile.RPKProfileName
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import java.util.concurrent.CompletableFuture

class CharacterGroupSwitchPriorityCommand(private val plugin: RPKPermissionsBukkit) : RPKCommandExecutor {

    class InvalidGroupFailure : CommandFailure()
    class GroupNotPresentFailure(val group: RPKGroup) : CommandFailure()
    class NoCharacterFailure : CommandFailure()

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<CommandResult> {
        if (!sender.hasPermission("rpkit.permissions.command.group.switchpriority")) {
            sender.sendMessage(plugin.messages.noPermissionCharacterGroupSwitchPriority)
            return CompletableFuture.completedFuture(NoPermissionFailure("rpkit.permissions.command.group.switchpriority"))
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileService)
            return CompletableFuture.completedFuture(MissingServiceFailure(RPKMinecraftProfileService::class.java))
        }
        if (args.size < 4) {
            sender.sendMessage(plugin.messages.characterGroupSwitchPriorityUsage)
            return CompletableFuture.completedFuture(IncorrectUsageFailure())
        }
        val profileService = Services[RPKProfileService::class.java]
        if (profileService == null) {
            sender.sendMessage(plugin.messages.noProfileService)
            return CompletableFuture.completedFuture(MissingServiceFailure(RPKProfileService::class.java))
        }
        if (!args[0].contains("#")) {
            sender.sendMessage(plugin.messages.characterGroupSwitchPriorityInvalidProfileName)
            return CompletableFuture.completedFuture(IncorrectUsageFailure())
        }
        val nameParts = args[0].split("#")
        val name = nameParts[0]
        val discriminator = nameParts[1].toIntOrNull()
        if (discriminator == null) {
            sender.sendMessage(plugin.messages.characterGroupSwitchPriorityInvalidProfileName)
            return CompletableFuture.completedFuture(IncorrectUsageFailure())
        }
        return profileService.getProfile(RPKProfileName(name), RPKProfileDiscriminator(discriminator)).thenApplyAsync getProfile@{ profile ->
            if (profile == null) {
                sender.sendMessage(plugin.messages.characterGroupSwitchPriorityInvalidProfile)
                return@getProfile InvalidTargetProfileFailure()
            }
            val characterService = Services[RPKCharacterService::class.java]
            if (characterService == null) {
                sender.sendMessage(plugin.messages.noCharacterService)
                return@getProfile MissingServiceFailure(RPKCharacterService::class.java)
            }
            return@getProfile characterService.getCharacters(profile).thenApplyAsync getCharacters@{ characters ->
                if (characters.isEmpty()) {
                    sender.sendMessage(plugin.messages.noCharacter)
                    return@getCharacters NoCharacterFailure()
                }
                val character = characters.minByOrNull { args.drop(1).dropLast(2).joinToString(" ").levenshtein(it.name) }
                if (character == null) {
                    sender.sendMessage(plugin.messages.noCharacter)
                    return@getCharacters NoCharacterFailure()
                }
                val groupService = Services[RPKGroupService::class.java]
                if (groupService == null) {
                    sender.sendMessage(plugin.messages.noGroupService)
                    return@getCharacters MissingServiceFailure(RPKGroupService::class.java)
                }
                val group1 = groupService.getGroup(RPKGroupName(args[2]))
                if (group1 == null) {
                    sender.sendMessage(
                        plugin.messages.characterGroupSwitchPriorityInvalidGroup.withParameters(
                            groupName = args[2]
                        )
                    )
                    return@getCharacters InvalidGroupFailure()
                }
                val group2 = groupService.getGroup(RPKGroupName(args[3]))
                if (group2 == null) {
                    sender.sendMessage(
                        plugin.messages.characterGroupSwitchPriorityInvalidGroup.withParameters(
                            groupName = args[3]
                        )
                    )
                    return@getCharacters InvalidGroupFailure()
                }
                val group1Priority = groupService.getGroupPriority(character, group1).join()
                if (group1Priority == null) {
                    sender.sendMessage(
                        plugin.messages.characterGroupSwitchPriorityInvalidGroupNotPresent.withParameters(
                            character = character,
                            group = group1
                        )
                    )
                    return@getCharacters GroupNotPresentFailure(group1)
                }
                val group2Priority = groupService.getGroupPriority(character, group2).join()
                if (group2Priority == null) {
                    sender.sendMessage(
                        plugin.messages.characterGroupSwitchPriorityInvalidGroupNotPresent.withParameters(
                            character = character,
                            group = group2
                        )
                    )
                    return@getCharacters GroupNotPresentFailure(group2)
                }
                groupService.setGroupPriority(character, group1, group2Priority).join()
                groupService.setGroupPriority(character, group2, group1Priority).join()
                sender.sendMessage(
                    plugin.messages.characterGroupSwitchPriorityValid.withParameters(
                        character = character,
                        group1 = group1,
                        group2 = group2
                    )
                )
                return@getCharacters CommandSuccess
            }.join()
        }
    }

}