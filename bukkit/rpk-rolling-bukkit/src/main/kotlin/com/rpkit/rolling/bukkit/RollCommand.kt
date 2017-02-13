package com.rpkit.rolling.bukkit

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.regex.Pattern


class RollCommand(private val plugin: RPKRollingBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.rolling.command.roll")) {
            if (sender is Player) {
                if (args.isNotEmpty()) {
                    val diePattern = Pattern.compile("[+-]\\d*d\\d+")
                    val fullRollString = if (args[0].startsWith("+")) args[0] else "+${args[0]}"
                    val dieMatcher = diePattern.matcher(fullRollString)
                    var total = 0
                    val parsedRollBuilder = StringBuilder()
                    while (dieMatcher.find()) {
                        val rollString = dieMatcher.group()
                        val multiplier = if (rollString.startsWith("-")) -1 else 1
                        val rollSections = rollString.split("d")
                        val diceAmountString = rollSections[0].drop(1)
                        val dieFaces = rollSections[1].toInt()
                        val diceAmount = if (diceAmountString.isEmpty()) 1 else diceAmountString.toInt()
                        val die = Die(dieFaces)
                        for (i in 1..diceAmount) {
                            total += multiplier * die.roll()
                        }
                        parsedRollBuilder.append(rollString)
                    }
                    val rollStringWithoutDice = fullRollString.replace(Regex("[+-]\\d*d\\d+"), "")
                    val literalPattern = Pattern.compile("([+-])(\\d+)(?!d)")
                    val literalMatcher = literalPattern.matcher(rollStringWithoutDice)
                    while (literalMatcher.find()) {
                        val sign = literalMatcher.group(1)
                        val amount = literalMatcher.group(2).toInt()
                        if (sign == "+") {
                            total += amount
                        } else if (sign == "-") {
                            total -= amount
                        }
                        parsedRollBuilder.append(sign + amount)
                    }
                    if (!parsedRollBuilder.isEmpty()) {
                        val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                        val player = playerProvider.getPlayer(sender)
                        val character = characterProvider.getActiveCharacter(player)
                        if (character != null) {
                            val radius = plugin.config.getInt("rolls.radius")
                            val parsedRoll = if (parsedRollBuilder.startsWith("+"))
                                parsedRollBuilder.toString().drop(1)
                            else
                                parsedRollBuilder.toString()
                            sender.world.players
                                    .filter { player -> player.location.distanceSquared(sender.location) <= radius * radius }
                                    .forEach {
                                        it.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.roll"))
                                                .replace("\$character", character.name)
                                                .replace("\$player", player.name)
                                                .replace("\$roll", total.toString())
                                                .replace("\$dice", parsedRoll))
                                    }
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-character")))
                        }
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.roll-invalid-parse")))
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.roll-usage")))
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
            }
        }
        return true
    }
}