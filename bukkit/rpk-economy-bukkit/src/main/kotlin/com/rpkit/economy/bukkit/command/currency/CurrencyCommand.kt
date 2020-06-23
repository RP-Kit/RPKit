/*
 * Copyright 2020 Ren Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rpkit.economy.bukkit.command.currency

import com.rpkit.economy.bukkit.RPKEconomyBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Currency command.
 * Parent command for all currency management commands.
 */
class CurrencyCommand(private val plugin: RPKEconomyBukkit): CommandExecutor {

    private val currencyAddCommand = CurrencyAddCommand(plugin)
    private val currencyRemoveCommand = CurrencyRemoveCommand(plugin)
    private val currencyListCommand = CurrencyListCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (args.isNotEmpty()) {
            val newArgs = args.drop(1).toTypedArray()
            if (args[0].equals("add", ignoreCase = true) || args[0].equals("create", ignoreCase = true) || args[0].equals("new", ignoreCase = true)) {
                return currencyAddCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("remove", ignoreCase = true) || args[0].equals("delete", ignoreCase = true)) {
                return currencyRemoveCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("list", ignoreCase = true)) {
                return currencyListCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(plugin.messages["currency-usage"])
            }
        } else {
            sender.sendMessage(plugin.messages["currency-usage"])
        }
        return true
    }

}
