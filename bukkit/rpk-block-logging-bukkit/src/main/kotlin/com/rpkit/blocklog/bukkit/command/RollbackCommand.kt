package com.rpkit.blocklog.bukkit.command

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryProvider
import com.rpkit.blocklog.bukkit.event.blocklog.RPKBukkitBlockRollbackEvent
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder


class RollbackCommand(private val plugin: RPKBlockLoggingBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!sender.hasPermission("rpkit.blocklogging.command.rollback")) {
            sender.sendMessage(plugin.messages["no-permission-rollback"])
            return true
        }
        if (args.size < 2) {
            sender.sendMessage(plugin.messages["rollback-usage"])
            return true
        }
        val blockHistoryProvider = plugin.core.serviceManager.getServiceProvider(RPKBlockHistoryProvider::class)
        val radius = args[0].toIntOrNull()
        if (radius == null || radius <= 0) {
            sender.sendMessage(plugin.messages["rollback-invalid-radius"])
            return true
        }
        val minutes = args[1].toIntOrNull()
        if (minutes == null || minutes <= 0) {
            sender.sendMessage(plugin.messages["rollback-invalid-time"])
            return true
        }
        val millis = minutes * 60000
        val time = System.currentTimeMillis() - millis
        for (x in (sender.location.blockX - radius)..(sender.location.blockX + radius)) {
            for (y in (sender.location.blockY - radius)..(sender.location.blockY + radius)) {
                for (z in (sender.location.blockZ - radius)..(sender.location.blockZ + radius)) {
                    val block = sender.world.getBlockAt(x, y, z)
                    val event = RPKBukkitBlockRollbackEvent(block, time)
                    plugin.server.pluginManager.callEvent(event)
                    if (event.isCancelled) continue
                    block.type = blockHistoryProvider.getBlockTypeAtTime(event.block, event.time)
                    val state = block.state
                    if (state is InventoryHolder) {
                        state.inventory.contents = blockHistoryProvider.getBlockInventoryAtTime(event.block, event.time)
                        state.update()
                    }
                }
            }
        }
        sender.sendMessage(plugin.messages["rollback-valid"])
        return true
    }

}