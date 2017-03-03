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
                                    sender.sendMessage(plugin.messages["distance-invalid-untrackable"])
                                    bukkitPlayer.sendMessage(plugin.messages["distance-untrackable-notification", mapOf(
                                            Pair("player", player.name)
                                    )])
                                    return true
                                }
                                val itemRequirement = plugin.config.getItemStack("distance-command.item-requirement")
                                if (itemRequirement != null && !bukkitPlayer.inventory.containsAtLeast(itemRequirement, itemRequirement.amount)) {
                                    sender.sendMessage(plugin.messages["distance-invalid-item", mapOf(
                                            Pair("amount", itemRequirement.amount.toString()),
                                            Pair("type", itemRequirement.type.toString().toLowerCase().replace('_', ' '))
                                    )])
                                    return true
                                }
                                val maximumDistance = plugin.config.getInt("distance-command.maximum-distance")
                                val distance = MathUtils.fastSqrt(bukkitPlayer.location.distanceSquared(sender.location))
                                if (maximumDistance >= 0 && distance > maximumDistance) {
                                    sender.sendMessage(plugin.messages["distance-invalid-distance"])
                                    return true
                                }
                                sender.sendMessage(plugin.messages["distance-valid", mapOf(
                                        Pair("character", character.name),
                                        Pair("player", player.name),
                                        Pair("distance", distance.toString())
                                )])
                            }
                        } else {
                            sender.sendMessage(plugin.messages["distance-invalid-world"])
                        }
                    } else {
                        sender.sendMessage(plugin.messages["distance-invalid-offline"])
                    }
                } else {
                    sender.sendMessage(plugin.messages["distance-usage"])
                }
            } else {
                sender.sendMessage(plugin.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-distance"])
        }
        return true
    }

}
