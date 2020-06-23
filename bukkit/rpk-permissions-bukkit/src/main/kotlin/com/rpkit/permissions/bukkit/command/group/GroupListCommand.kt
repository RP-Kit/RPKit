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

package com.rpkit.permissions.bukkit.command.group

import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.group.RPKGroupProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class GroupListCommand(private val plugin: RPKPermissionsBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.permissions.command.group.list")) {
            sender.sendMessage(plugin.messages["no-permission-group-list"])
            return true
        }
        sender.sendMessage(plugin.messages["group-list-title"])
        plugin.core.serviceManager.getServiceProvider(RPKGroupProvider::class).groups.forEach { group ->
            sender.sendMessage(plugin.messages["group-list-item", mapOf(Pair("group", group.name))])
        }
        return true
    }
}