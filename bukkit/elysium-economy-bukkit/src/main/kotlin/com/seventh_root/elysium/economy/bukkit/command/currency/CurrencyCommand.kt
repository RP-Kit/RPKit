package com.seventh_root.elysium.economy.bukkit.command.currency

import com.seventh_root.elysium.economy.bukkit.ElysiumEconomyBukkit
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class CurrencyCommand(private val plugin: ElysiumEconomyBukkit): CommandExecutor {
    private val currencyAddCommand: CurrencyAddCommand
    private val currencyRemoveCommand: CurrencyRemoveCommand
    private val currencyListCommand: CurrencyListCommand

    init {
        this.currencyAddCommand = CurrencyAddCommand(plugin)
        this.currencyRemoveCommand = CurrencyRemoveCommand(plugin)
        this.currencyListCommand = CurrencyListCommand(plugin)
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.size > 0) {
            var newArgsList: MutableList<String> = arrayListOf()
            for (i: Int in 1..(args.size - 1)) {
                newArgsList.add(args[i])
            }
            var newArgs = newArgsList.toTypedArray()
            if (args[0].equals("add", ignoreCase = true) || args[0].equals("create", ignoreCase = true) || args[0].equals("new", ignoreCase = true)) {
                return currencyAddCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("remove", ignoreCase = true) || args[0].equals("delete", ignoreCase = true)) {
                return currencyRemoveCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("list", ignoreCase = true)) {
                return currencyListCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.currency-usage")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.currency-usage")))
        }
        return true
    }

}
