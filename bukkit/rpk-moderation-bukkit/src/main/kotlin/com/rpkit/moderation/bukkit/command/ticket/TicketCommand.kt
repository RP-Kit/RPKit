/*
 * Copyright 2018 Ross Binden
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

package com.rpkit.moderation.bukkit.command.ticket

import com.rpkit.moderation.bukkit.RPKModerationBukkit
import com.rpkit.moderation.bukkit.command.ticket.list.TicketListCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class TicketCommand(private val plugin: RPKModerationBukkit): CommandExecutor {

    private val ticketCreateCommand = TicketCreateCommand(plugin)
    private val ticketCloseCommand = TicketCloseCommand(plugin)
    private val ticketListCommand = TicketListCommand(plugin)
    private val ticketTeleportCommand = TicketTeleportCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["ticket-usage"])
            return true
        }
        return when (args[0]) {
            "create" -> ticketCreateCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            "close" -> ticketCloseCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            "list" -> ticketListCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            "teleport", "tp" -> ticketTeleportCommand.onCommand(sender, command, label, args.drop(1).toTypedArray())
            else -> {
                sender.sendMessage(plugin.messages["ticket-usage"])
                true
            }
        }
    }
}