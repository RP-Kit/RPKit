package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import java.text.SimpleDateFormat
import java.util.*

class SeenCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.seen")) {
            if (args.isNotEmpty()) {
                val player = plugin.server.getOfflinePlayer(args[0])
                if (player.isOnline) {
                    sender.sendMessage(plugin.core.messages["seen-online", mapOf(
                            Pair("player", player.name)
                    )])
                } else {
                    if (player.lastPlayed != 0L) {
                        val lastPlayed = Date(player.lastPlayed)
                        sender.sendMessage(plugin.core.messages["seen-date", mapOf(
                                Pair("player", player.name),
                                Pair("date", SimpleDateFormat("yyyy-MM-dd").format(lastPlayed)),
                                Pair("time", SimpleDateFormat("HH:mm:ss").format(lastPlayed))
                        )])
                        val millis = System.currentTimeMillis() - player.lastPlayed
                        val second = millis / 1000 % 60
                        val minute = millis / (1000 * 60) % 60
                        val hour = millis / (1000 * 60 * 60) % 24
                        val day = millis / (1000 * 60 * 60 * 24)
                        sender.sendMessage(plugin.core.messages["seen-diff", mapOf(
                                Pair("days", day.toString()),
                                Pair("hours", hour.toString()),
                                Pair("minutes", minute.toString()),
                                Pair("seconds", second.toString())
                        )])
                    } else {
                        sender.sendMessage(plugin.core.messages["seen-never"])
                    }
                }
            } else {
                sender.sendMessage(plugin.core.messages["seen-usage"])
            }
        } else {
            sender.sendMessage(plugin.core.messages["no-permission-seen"])
        }
        return true
    }
}
