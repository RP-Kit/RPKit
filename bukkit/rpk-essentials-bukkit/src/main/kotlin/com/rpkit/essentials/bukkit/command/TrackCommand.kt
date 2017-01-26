package com.rpkit.essentials.bukkit.command

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import com.rpkit.tracking.bukkit.tracking.RPKTrackingProvider
import org.bukkit.ChatColor
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
                        val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                        val trackingProvider = plugin.core.serviceManager.getServiceProvider(RPKTrackingProvider::class)
                        val player = playerProvider.getPlayer(bukkitPlayer)
                        val character = characterProvider.getActiveCharacter(player)
                        if (character != null) {
                            if (!trackingProvider.isTrackable(character)) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.track-invalid-untrackable")))
                                bukkitPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.track-untrackable-notification"))
                                        .replace("\$player", sender.name))
                                return true
                            }
                            val itemRequirement = plugin.config.getItemStack("track-command.item-requirement")
                            if (itemRequirement != null && !bukkitPlayer.inventory.containsAtLeast(itemRequirement, itemRequirement.amount)) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.track-invalid-item"))
                                        .replace("\$amount", itemRequirement.amount.toString())
                                        .replace("\$type", itemRequirement.type.toString().toLowerCase().replace('_', ' ')))
                                return true
                            }
                            val maximumDistance = plugin.config.getInt("track-command.maximum-distance")
                            val distanceSquared = bukkitPlayer.location.distanceSquared(sender.location)
                            if (maximumDistance >= 0 && distanceSquared > maximumDistance * maximumDistance) {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.track-invalid-distance")))
                                return true
                            }
                            sender.compassTarget = bukkitPlayer.location
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.track-valid"))
                                    .replace("\$player", player.name)
                                    .replace("\$character", if (character.isNameHidden) "[HIDDEN]" else character.name))
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character-other")))
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.track-invalid-player")))
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.track-usage")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-track")))
        }
        return true
    }

}
