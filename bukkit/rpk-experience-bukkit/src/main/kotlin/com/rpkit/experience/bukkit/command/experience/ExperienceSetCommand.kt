package com.rpkit.experience.bukkit.command.experience

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.experience.bukkit.RPKExperienceBukkit
import com.rpkit.experience.bukkit.experience.RPKExperienceProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class ExperienceSetCommand(private val plugin: RPKExperienceBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.experience.command.experience.set")) {
            if (args.size >= 2) {
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val experienceProvider = plugin.core.serviceManager.getServiceProvider(RPKExperienceProvider::class)
                val bukkitPlayer = plugin.server.getPlayer(args[0])
                if (bukkitPlayer != null) {
                    val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
                    if (minecraftProfile != null) {
                        val character = characterProvider.getActiveCharacter(minecraftProfile)
                        if (character != null) {
                            try {
                                val experience = args[1].toInt()
                                experienceProvider.setExperience(character, experience)
                                sender.sendMessage(plugin.messages["experience-set-valid"])
                            } catch (exception: NumberFormatException) {
                                sender.sendMessage(plugin.messages["experience-set-experience-invalid-number"])
                            }
                        } else {
                            sender.sendMessage(plugin.messages["no-character-other"])
                        }
                    } else {
                        sender.sendMessage(plugin.messages["no-minecraft-profile"])
                    }
                } else {
                    sender.sendMessage(plugin.messages["experience-set-player-invalid-player"])
                }
            } else {
                sender.sendMessage(plugin.messages["experience-set-usage"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-experience-set"])
        }
        return true
    }
}