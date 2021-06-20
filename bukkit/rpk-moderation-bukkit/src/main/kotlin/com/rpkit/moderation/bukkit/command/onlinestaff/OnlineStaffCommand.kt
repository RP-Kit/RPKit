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

package com.rpkit.moderation.bukkit.command.onlinestaff

import com.rpkit.moderation.bukkit.RPKModerationBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class OnlineStaffCommand(private val plugin: RPKModerationBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.moderation.command.onlinestaff")) {
            sender.sendMessage(plugin.messages["online-staff-title"])
            plugin.server.onlinePlayers
                    .filter { player -> player.hasPermission("rpkit.moderation.staff") }
                    .forEach { player ->
                        sender.sendMessage(plugin.messages["online-staff-item", mapOf("name" to player.name)])
                    }
        }
        return true
    }
}