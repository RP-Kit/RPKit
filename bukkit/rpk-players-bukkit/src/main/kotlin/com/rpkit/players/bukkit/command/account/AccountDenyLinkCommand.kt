package com.rpkit.players.bukkit.command.account

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfileImpl
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class AccountDenyLinkCommand(private val plugin: RPKPlayersBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (args.size <= 1) {
            sender.sendMessage(plugin.messages["account-deny-link-usage"])
            return true
        }
        val type = args[0]
        val id = args[1].toIntOrNull()
        when (type.toLowerCase()) {
            "minecraft" -> {
                if (id == null) {
                    sender.sendMessage(plugin.messages["account-deny-link-invalid-id"])
                    return true
                }
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
                if (minecraftProfile == null) {
                    sender.sendMessage(plugin.messages["no-minecraft-profile"])
                    return true
                }
                val linkRequests = minecraftProfileProvider.getMinecraftProfileLinkRequests(minecraftProfile)
                val linkRequest = linkRequests.firstOrNull { request -> request.profile.id == id }
                if (linkRequest == null) {
                    sender.sendMessage(plugin.messages["account-deny-link-invalid-request"])
                    return true
                }
                minecraftProfileProvider.removeMinecraftProfileLinkRequest(linkRequest)
                if (linkRequests.isNotEmpty()) {
                    sender.sendMessage(plugin.messages["account-deny-link-valid"])
                    return true
                }
                // If they no longer have any link requests pending, we can create a new profile for them based on their
                // Minecraft profile.
                val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
                val profile = RPKProfileImpl(
                        minecraftProfile.minecraftUsername,
                        ""
                )
                profileProvider.addProfile(profile)
                minecraftProfile.profile = profile
                minecraftProfileProvider.updateMinecraftProfile(minecraftProfile)
                sender.sendMessage(plugin.messages["account-deny-link-profile-created"])
                return true
            }
            else -> {
                sender.sendMessage(plugin.messages["account-deny-link-invalid-type"])
                return true
            }
        }
    }
}