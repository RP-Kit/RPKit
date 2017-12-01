package com.rpkit.blocklog.bukkit.command

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryProvider
import com.rpkit.blocklog.bukkit.block.RPKBlockInventoryChange
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.text.SimpleDateFormat
import java.util.*


class InventoryHistoryCommand(private val plugin: RPKBlockLoggingBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!sender.hasPermission("rpkit.blocklogging.command.history")) {
            sender.sendMessage(plugin.messages["no-permission-inventory-history"])
            return true
        }
        val targetBlock = sender.getTargetBlock(null as? HashSet<Material>, 8)
        if (targetBlock == null) {
            sender.sendMessage(plugin.messages["inventory-history-no-target-block"])
            return true
        }
        val blockHistoryProvider = plugin.core.serviceManager.getServiceProvider(RPKBlockHistoryProvider::class)
        val blockHistory = blockHistoryProvider.getBlockHistory(targetBlock)
        val changes = blockHistory.inventoryChanges
        if (changes.isEmpty()) {
            sender.sendMessage(plugin.messages["inventory-history-no-changes"])
            return true
        }
        for (change in changes.sortedBy(RPKBlockInventoryChange::time).take(100)) {
            sender.sendMessage(plugin.messages["inventory-history-change", mapOf(
                    Pair("time", SimpleDateFormat("yyyy-MM-dd HH:mm:ss zzz").format(Date(change.time))),
                    Pair("profile", change.profile?.name?:"None"),
                    Pair("minecraft-profile", change.minecraftProfile?.minecraftUsername?:"None"),
                    Pair("character", change.character?.name?:"None"),
                    Pair("from", Arrays.toString(change.from)),
                    Pair("to", Arrays.toString(change.to)),
                    Pair("reason", change.reason)
            )])
        }
        return true
    }

}