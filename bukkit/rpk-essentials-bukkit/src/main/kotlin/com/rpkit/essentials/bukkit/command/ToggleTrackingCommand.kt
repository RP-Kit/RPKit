package com.rpkit.essentials.bukkit.command

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.tracking.bukkit.tracking.RPKTrackingProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ToggleTrackingCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Player) {
            if (sender.hasPermission("rpkit.essentials.command.toggletracking")) {
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val trackingProvider = plugin.core.serviceManager.getServiceProvider(RPKTrackingProvider::class)
                val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
                if (minecraftProfile != null) {
                    val character = characterProvider.getActiveCharacter(minecraftProfile)
                    if (character != null) {
                        trackingProvider.setTrackable(character, !trackingProvider.isTrackable(character))
                        if (trackingProvider.isTrackable(character)) {
                            sender.sendMessage(plugin.messages["toggle-tracking-on-valid"])
                        } else {
                            sender.sendMessage(plugin.messages["toggle-tracking-off-valid"])
                        }
                    } else {
                        sender.sendMessage(plugin.messages["no-character-self"])
                    }
                } else {
                    sender.sendMessage(plugin.messages["no-minecraft-profile"])
                }
            } else {
                sender.sendMessage(plugin.messages["no-permission-toggle-tracking"])
            }
        } else {
            sender.sendMessage(plugin.messages["not-from-console"])
        }
        return true
    }
}
