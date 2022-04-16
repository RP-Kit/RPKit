package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.Material
import org.bukkit.block.CreatureSpawner
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

class SpawnerCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.spawner")) {
            if (args.isNotEmpty()) {
                if (sender is Player) {
                    val transparent: Set<Material>? = null
                    val block = sender.getTargetBlock(transparent, 32)
                    if (block.type == Material.SPAWNER) {
                        val spawner = block.state as CreatureSpawner
                        try {
                            val entityType = EntityType.valueOf(args[0].uppercase())
                            spawner.spawnedType = entityType
                            sender.sendMessage(plugin.messages["spawner-valid"])
                        } catch (exception: IllegalArgumentException) {
                            sender.sendMessage(plugin.messages["spawner-invalid-entity"])
                        }
                    } else {
                        sender.sendMessage(plugin.messages["spawner-invalid-block"])
                    }
                } else {
                    sender.sendMessage(plugin.messages["not-from-console"])
                }
            } else {
                sender.sendMessage(plugin.messages["spawner-usage"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-spawner"])
        }
        return true
    }

}
