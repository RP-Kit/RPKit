/*
 * Copyright 2016 Ross Binden
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

package com.rpkit.players.bukkit.command.account

import com.rpkit.players.bukkit.RPKPlayersBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Account link command.
 * Links another account to the current player.
 */
class AccountLinkCommand(private val plugin: RPKPlayersBukkit): CommandExecutor {

    private val accountLinkIRCCommand = AccountLinkIRCCommand(plugin)
    private val accountLinkMinecraftCommand = AccountLinkMinecraftCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.players.command.account.link")) {
            sender.sendMessage(plugin.messages["no-permission-account-link"])
            return true
        }
        if (args.isNotEmpty()) {
            val newArgs = args.drop(1).toTypedArray()
            if (args[0].equals("irc", ignoreCase = true)) {
                return accountLinkIRCCommand.onCommand(sender, command, label, newArgs)
            } else if (args[0].equals("minecraft", ignoreCase = true) || args[0].equals("mc", ignoreCase = true)) {
                return accountLinkMinecraftCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(plugin.messages["account-link-usage"])
            }
        } else {
            sender.sendMessage(plugin.messages["account-link-usage"])
        }
        return true
    }

}
