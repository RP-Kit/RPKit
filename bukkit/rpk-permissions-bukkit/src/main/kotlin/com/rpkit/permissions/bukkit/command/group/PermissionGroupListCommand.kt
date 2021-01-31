package com.rpkit.permissions.bukkit.command.group

import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.group.RPKGroupService
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PermissionGroupListCommand(private val plugin: RPKPermissionsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!sender.hasPermission("rpkit.permissions.command.character.group.list")) {
            sender.sendMessage(plugin.messages["no-permission-character-group-list"])
            return true
        }

        var playerUUID = sender.uniqueId;

        if (args.isNotEmpty()) {
            val player = Bukkit.getPlayer(args[0]);
            if (player != null) {
                playerUUID = Bukkit.getPlayer(args[0])!!.uniqueId;
            } else {
                sender.sendMessage(plugin.messages["no-player"])
                return true
            }
        }

        val rpkGroupService = Services[RPKGroupService::class.java]
        if (rpkGroupService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }

        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }

        val minecraftProfile = minecraftProfileService.getMinecraftProfile(playerUUID)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }

        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages["no-profile"])
            return true
        }

        sender.sendMessage(plugin.messages["character-group-list-title"])
        for (group in rpkGroupService.getGroups(profile)) {
            sender.sendMessage(
                plugin.messages["group-list-item", mapOf(
                    "group" to group.name.value
                )]
            )
        }
        return true
    }
}
