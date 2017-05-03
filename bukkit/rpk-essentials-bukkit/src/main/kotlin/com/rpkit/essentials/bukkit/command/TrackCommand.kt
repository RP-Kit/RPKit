package com.rpkit.essentials.bukkit.command

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.tracking.bukkit.tracking.RPKTrackingProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class TrackCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.track")) {
            if (sender is Player) {
                if (args.isNotEmpty()) {
                    val bukkitPlayer = plugin.server.getPlayer(args[0])
                    if (bukkitPlayer != null) {
                        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                        val trackingProvider = plugin.core.serviceManager.getServiceProvider(RPKTrackingProvider::class)
                        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
                        if (minecraftProfile != null) {
                            val character = characterProvider.getActiveCharacter(minecraftProfile)
                            if (character != null) {
                                if (!trackingProvider.isTrackable(character)) {
                                    sender.sendMessage(plugin.messages["track-invalid-untrackable"])
                                    bukkitPlayer.sendMessage(plugin.messages["track-untrackable-notification", mapOf(
                                            Pair("player", sender.name)
                                    )])
                                    return true
                                }
                                val itemRequirement = plugin.config.getItemStack("track-command.item-requirement")
                                if (itemRequirement != null && !bukkitPlayer.inventory.containsAtLeast(itemRequirement, itemRequirement.amount)) {
                                    sender.sendMessage(plugin.messages["track-invalid-item", mapOf(
                                            Pair("amount", itemRequirement.amount.toString()),
                                            Pair("type", itemRequirement.type.toString().toLowerCase().replace('_', ' '))
                                    )])
                                    return true
                                }
                                val maximumDistance = plugin.config.getInt("track-command.maximum-distance")
                                val distanceSquared = bukkitPlayer.location.distanceSquared(sender.location)
                                if (maximumDistance >= 0 && distanceSquared > maximumDistance * maximumDistance) {
                                    sender.sendMessage(plugin.messages["track-invalid-distance"])
                                    return true
                                }
                                sender.compassTarget = bukkitPlayer.location
                                sender.sendMessage(plugin.messages["track-valid", mapOf(
                                        Pair("player", minecraftProfile.minecraftUsername),
                                        Pair("character", if (character.isNameHidden) "[HIDDEN]" else character.name)
                                )])
                            } else {
                                sender.sendMessage(plugin.messages["no-character-other"])
                            }
                        } else {
                            sender.sendMessage(plugin.messages["no-minecraft-profile"])
                        }
                    } else {
                        sender.sendMessage(plugin.messages["track-invalid-player"])
                    }
                } else {
                    sender.sendMessage(plugin.messages["track-usage"])
                }
            } else {
                sender.sendMessage(plugin.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-track"])
        }
        return true
    }

}
