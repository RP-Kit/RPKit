package com.seventh_root.elysium.characters.bukkit.command.gender

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.gender.ElysiumGenderProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class GenderListCommand(private val plugin: ElysiumCharactersBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("elysium.characters.command.gender.list")) {
            val genderProvider = plugin.core.serviceManager.getServiceProvider(ElysiumGenderProvider::class.java)
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.gender-list-title")))
            for (gender in genderProvider.genders) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.gender-list-item"))
                        .replace("\$gender", gender.name))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-gender-list")))
        }
        return true
    }

}
