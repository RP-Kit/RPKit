/*
 * Copyright 2021 Ren Binden
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

import com.rpkit.core.service.Services
import com.rpkit.moderation.bukkit.RPKModerationBukkit
import com.rpkit.moderation.bukkit.ticket.RPKTicketId
import com.rpkit.moderation.bukkit.ticket.RPKTicketService
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class TicketCloseCommand(private val plugin: RPKModerationBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["ticket-close-usage"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val profile = minecraftProfile.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages["no-profile"])
            return true
        }
        val ticketService = Services[RPKTicketService::class.java]
        if (ticketService == null) {
            sender.sendMessage(plugin.messages["no-ticket-service"])
            return true
        }
        try {
            val ticket = ticketService.getTicket(RPKTicketId(args[0].toInt()))
            if (ticket == null) {
                sender.sendMessage(plugin.messages["ticket-close-invalid-ticket"])
                return true
            }
            if (!sender.hasPermission("rpkit.moderation.command.ticket.close") && ticket.issuer.id != profile.id) {
                sender.sendMessage(plugin.messages["no-permission-ticket-close"])
                return true
            }
            ticket.close(profile)
            ticketService.updateTicket(ticket)
            sender.sendMessage(plugin.messages["ticket-close-valid"])
        } catch (exception: NumberFormatException) {
            sender.sendMessage(plugin.messages["ticket-close-invalid-id"])
        }
        return true
    }

}