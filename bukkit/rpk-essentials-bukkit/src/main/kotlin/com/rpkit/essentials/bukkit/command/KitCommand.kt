package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.kit.bukkit.kit.RPKKit
import com.rpkit.kit.bukkit.kit.RPKKitProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class KitCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.kit")) {
            if (sender is Player) {
                val kitProvider = plugin.core.serviceManager.getServiceProvider(RPKKitProvider::class)
                if (args.isNotEmpty()) {
                    val kit = kitProvider.getKit(args[0])
                    if (kit != null) {
                        sender.inventory.addItem(*kit.items.toTypedArray())
                        sender.sendMessage(plugin.core.messages["kit-valid", mapOf(
                                Pair("kit", kit.name)
                        )])
                    } else {
                        sender.sendMessage(plugin.core.messages["kit-invalid-kit"])
                    }
                } else {
                    sender.sendMessage(plugin.core.messages["kit-list-title"])
                    for (kitName in kitProvider.kits.map(RPKKit::name)) {
                        sender.sendMessage(plugin.core.messages["kit-list-item", mapOf(
                                Pair("kit", kitName)
                        )])
                    }
                }
            } else {
                sender.sendMessage(plugin.core.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.core.messages["no-permission-kit"])
        }
        return true
    }

}
