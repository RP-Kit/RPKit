package com.seventh_root.elysium.economy.bukkit.command.money

import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class MoneyCommand(val plugin: ElysiumEconomyBukkit): CommandExecutor {

    private val moneySubtractCommand = MoneySubtractCommand(plugin)
    private val moneyAddCommand = MoneyAddCommand(plugin)
    private val moneySetCommand = MoneySetCommand(plugin)
    private val moneyViewCommand = MoneyViewCommand(plugin)
    private val moneyPayCommand = MoneyPayCommand(plugin)
    private val moneyWalletCommand = MoneyWalletCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.size > 0) {
            val newArgsList: MutableList<String> = arrayListOf()
            for (i: Int in 1..(args.size - 1)) {
                newArgsList.add(args[i])
            }
            val newArgs = newArgsList.toTypedArray()
            if (args[0].equals("subtract", ignoreCase = true) || args[0].equals("sub", ignoreCase = true)) {
                return moneySubtractCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("add", ignoreCase = true)) {
                return moneyAddCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("set", ignoreCase = true)) {
                return moneySetCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("view", ignoreCase = true)) {
                return moneyViewCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("pay", ignoreCase = true)) {
                return moneyPayCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("wallet", ignoreCase = true)) {
                return moneyWalletCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.money-usage")))
            }
        } else {
            return moneyViewCommand.onCommand(sender, command, label, arrayOf<String>())
        }
        return true
    }

}