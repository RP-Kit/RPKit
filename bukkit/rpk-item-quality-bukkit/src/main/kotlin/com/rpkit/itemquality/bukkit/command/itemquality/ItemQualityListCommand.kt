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

package com.rpkit.itemquality.bukkit.command.itemquality

import com.rpkit.itemquality.bukkit.RPKItemQualityBukkit
import com.rpkit.itemquality.bukkit.itemquality.RPKItemQualityProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class ItemQualityListCommand(private val plugin: RPKItemQualityBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.itemquality.command.itemquality.list")) {
            sender.sendMessage(plugin.messages["no-permission-itemquality-list"])
            return true
        }
        sender.sendMessage(plugin.messages["itemquality-list-title"])
        val itemQualityProvider = plugin.core.serviceManager.getServiceProvider(RPKItemQualityProvider::class)
        itemQualityProvider.itemQualities.forEach { quality ->
            sender.sendMessage(plugin.messages["itemquality-list-item", mapOf(
                    Pair("quality", quality.name)
            )])
        }
        return true
    }
}