package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.ChatColor
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
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.seen-online"))
                            .replace("\$player", player.name))
                } else {
                    if (player.lastPlayed != 0L) {
                        val lastPlayed = Date(player.lastPlayed)
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.seen-date"))
                                .replace("\$player", player.name)
                                .replace("\$date", SimpleDateFormat("yyyy-MM-dd").format(lastPlayed))
                                .replace("\$time", SimpleDateFormat("HH:mm:ss").format(lastPlayed)))
                        val millis = System.currentTimeMillis() - player.lastPlayed
                        val second = millis / 1000 % 60
                        val minute = millis / (1000 * 60) % 60
                        val hour = millis / (1000 * 60 * 60) % 24
                        val day = millis / (1000 * 60 * 60 * 24)
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.seen-diff"))
                                .replace("\$days", day.toString())
                                .replace("\$hours", hour.toString())
                                .replace("\$minutes", minute.toString())
                                .replace("\$seconds", second.toString()))
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.seen-never")))
                    }
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.seen-usage")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-seen")))
        }
        return true
    }
}
