package com.rpkit.players.bukkit.command.profile

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfileImpl
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class ProfileCreateCommand(private val plugin: RPKPlayersBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!sender.hasPermission("rpkit.players.command.profile.create")) {
            sender.sendMessage(plugin.messages["no-permission-profile-create"])
            return true
        }
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val profile = minecraftProfile.profile
        if (profile != null) {
            sender.sendMessage(plugin.messages["profile-create-invalid-profile"])
            return true
        }
        if (args.size < 2) {
            sender.sendMessage(plugin.messages["profile-create-usage"])
            return true
        }
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val name = args[0]
        val password = args[1]
        val newProfile = RPKProfileImpl(name, password)
        profileProvider.addProfile(newProfile)
        minecraftProfile.profile = newProfile
        minecraftProfileProvider.updateMinecraftProfile(minecraftProfile)
        sender.sendMessage(plugin.messages["profile-create-valid"])
        return true
    }
}