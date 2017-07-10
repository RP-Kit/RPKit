package com.rpkit.players.bukkit.command.profile

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class ProfileLoginCommand(private val plugin: RPKPlayersBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!sender.hasPermission("rpkit.players.command.profile.login")) {
            sender.sendMessage(plugin.messages["no-permission-profile-login"])
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
            sender.sendMessage(plugin.messages["profile-login-invalid-profile-already-set"])
            return true
        }
        if (args.size < 2) {
            sender.sendMessage(plugin.messages["profile-login-usage"])
            return true
        }
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val name = args[0]
        val password = args[1]
        val loginProfile = profileProvider.getProfile(name)
        if (loginProfile == null) {
            sender.sendMessage(plugin.messages["profile-login-invalid-profile"])
            return true
        }
        if (!loginProfile.checkPassword(password.toCharArray())) {
            sender.sendMessage(plugin.messages["profile-login-invalid-password"])
            return true
        }
        minecraftProfile.profile = loginProfile
        minecraftProfileProvider.updateMinecraftProfile(minecraftProfile)
        sender.sendMessage(plugin.messages["profile-login-valid"])
        return true
    }
}