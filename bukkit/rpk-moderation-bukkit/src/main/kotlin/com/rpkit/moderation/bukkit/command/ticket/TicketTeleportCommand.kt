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
import com.rpkit.moderation.bukkit.ticket.RPKTicketProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class TicketTeleportCommand(private val plugin: RPKModerationBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.moderation.command.ticket.teleport")) {
            sender.sendMessage(plugin.messages["no-permission-ticket-teleport"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["ticket-teleport-usage"])
        }
        val ticketProvider = plugin.core.serviceManager.getServiceProvider(RPKTicketProvider::class)
        try {
            val ticket = ticketProvider.getTicket(args[0].toInt())
            if (ticket == null) {
                sender.sendMessage(plugin.messages["ticket-teleport-invalid-ticket"])
                return true
            }
            sender.teleport(ticket.location)
            sender.sendMessage(plugin.messages["ticket-teleport-valid"])
        } catch (exception: NumberFormatException) {
            sender.sendMessage(plugin.messages["ticket-teleport-usage"])
        }
        return true
    }

}