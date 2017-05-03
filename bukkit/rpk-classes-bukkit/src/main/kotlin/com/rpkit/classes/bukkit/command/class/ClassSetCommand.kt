package com.rpkit.classes.bukkit.command.`class`

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.classes.bukkit.classes.RPKClassProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class ClassSetCommand(private val plugin: RPKClassesBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.classes.command.class.set")) {
            sender.sendMessage(plugin.messages["no-permission-class-set"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["class-set-usage"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val classProvider = plugin.core.serviceManager.getServiceProvider(RPKClassProvider::class)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val character = characterProvider.getActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages["no-character"])
            return true
        }
        val className = args[0]
        val clazz = classProvider.getClass(className)
        if (clazz == null) {
            sender.sendMessage(plugin.messages["class-set-invalid-class"])
            return true
        }
        if (!clazz.hasPrerequisites(character)) {
            sender.sendMessage(plugin.messages["class-set-invalid-prerequisites"])
            return true
        }
        classProvider.setClass(character, clazz)
        sender.sendMessage(plugin.messages["class-set-valid", mapOf(
                Pair("class", clazz.name)
        )])
        return true
    }
}