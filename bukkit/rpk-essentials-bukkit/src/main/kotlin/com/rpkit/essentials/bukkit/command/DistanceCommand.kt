package com.rpkit.essentials.bukkit.command

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.util.MathUtils
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import com.rpkit.tracking.bukkit.tracking.RPKTrackingProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DistanceCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.distance")) {
            if (sender is Player) {
                if (args.isNotEmpty()) {
                    val bukkitPlayer = plugin.server.getPlayer(args[0])
                    if (bukkitPlayer != null) {
                        if (bukkitPlayer.world === sender.world) {
                            val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                            val trackingProvider = plugin.core.serviceManager.getServiceProvider(RPKTrackingProvider::class)
                            val player = playerProvider.getPlayer(bukkitPlayer)
                            val character = characterProvider.getActiveCharacter(player)
                            if (character != null) {
                                if (!trackingProvider.isTrackable(character)) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.distance-invalid-untrackable")))
                                    bukkitPlayer.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.distance-untrackable-notification"))
                                            .replace("\$player", player.name))
                                    return true
                                }
                                val itemRequirement = plugin.config.getItemStack("distance-command.item-requirement")
                                if (itemRequirement != null && !bukkitPlayer.inventory.containsAtLeast(itemRequirement, itemRequirement.amount)) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.distance-invalid-item"))
                                            .replace("\$amount", itemRequirement.amount.toString())
                                            .replace("\$type", itemRequirement.type.toString().toLowerCase().replace('_', ' ')))
                                    return true
                                }
                                val maximumDistance = plugin.config.getInt("distance-command.maximum-distance")
                                val distance = MathUtils.fastSqrt(bukkitPlayer.location.distanceSquared(sender.location))
                                if (maximumDistance >= 0 && distance > maximumDistance) {
                                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.distance-invalid-distance")))
                                    return true
                                }
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.distance-valid"))
                                        .replace("\$character", character.name)
                                        .replace("\$player", player.name)
                                        .replace("\$distance", distance.toString()))
                            }
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.distance-invalid-world")))
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.distance-invalid-offline")))
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messsages.distance-usage")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-distance")))
        }
        return true
    }

}
