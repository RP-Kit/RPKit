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

package com.rpkit.moderation.bukkit.command.ticket.list

import com.rpkit.moderation.bukkit.RPKModerationBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class TicketListCommand(private val plugin: RPKModerationBukkit) : CommandExecutor {

    private val ticketListOpenCommand = TicketListOpenCommand(plugin)
    private val ticketListClosedCommand = TicketListClosedCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.moderation.command.ticket.list")) {
            sender.sendMessage(plugin.messages["no-permission-ticket-list"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["ticket-list-usage"])
            return true
        }
        return when (args[0]) {
            "open" -> ticketListOpenCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            "closed" -> ticketListClosedCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            else -> {
                sender.sendMessage(plugin.messages["ticket-list-usage"])
                true
            }
        }
    }

}