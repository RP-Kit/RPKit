package com.rpkit.permissions.bukkit.command.group

import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.group.RPKGroupService
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class GroupViewCommand(private val plugin: RPKPermissionsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return true
        }
        if (!sender.hasPermission("rpkit.permissions.command.group.view")) {
            sender.sendMessage(plugin.messages.noPermissionGroupView)
            return true
        }

        var playerUUID = sender.uniqueId;

        if (args.isNotEmpty()) {
            val player = plugin.server.getPlayer(args[0]);
            if (player != null) {
                playerUUID = player.uniqueId;
            } else {
                sender.sendMessage(plugin.messages.groupViewInvalidPlayer)
                return true
            }
        }

        val groupService = Services[RPKGroupService::class.java]
        if (groupService == null) {
            sender.sendMessage(plugin.messages.noGroupService)
            return true
        }

        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileService)
            return true
        }

        val minecraftProfile = minecraftProfileService.getMinecraftProfile(playerUUID)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfile)
            return true
        }

        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages.noProfile)
            return true
        }

        sender.sendMessage(plugin.messages.groupViewTitle.withParameters(
            profile = profile
        ))
        for (group in groupService.getGroups(profile)) {
            sender.sendMessage(
                plugin.messages.groupViewItem.withParameters(
                    group = group
                )
            )
        }
        return true
    }
}
