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

package com.rpkit.essentials.bukkit.command

import com.rpkit.core.service.Services
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.kit.bukkit.kit.RPKKit
import com.rpkit.kit.bukkit.kit.RPKKitName
import com.rpkit.kit.bukkit.kit.RPKKitService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class KitCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission("rpkit.essentials.command.kit")) {
            sender.sendMessage(plugin.messages["no-permission-kit"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val kitService = Services[RPKKitService::class.java]
        if (kitService == null) {
            sender.sendMessage(plugin.messages["no-kit-service"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["kit-list-title"])
            for (kitName in kitService.kits.map(RPKKit::name)) {
                sender.sendMessage(plugin.messages["kit-list-item", mapOf(
                        "kit" to kitName.value
                )])
            }
            return true
        }
        val kit = kitService.getKit(RPKKitName(args[0]))
        if (kit == null) {
            sender.sendMessage(plugin.messages["kit-invalid-kit"])
            return true
        }
        sender.inventory.addItem(*kit.items.toTypedArray())
        sender.sendMessage(plugin.messages["kit-valid", mapOf(
                "kit" to kit.name.value
        )])
        return true
    }

}
