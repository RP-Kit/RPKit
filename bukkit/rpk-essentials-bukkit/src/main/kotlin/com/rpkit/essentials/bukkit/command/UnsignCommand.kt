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

package com.rpkit.essentials.bukkit.command

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.BookMeta

class UnsignCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.unsign")) {
            if (sender is Player) {
                if (sender.inventory.itemInMainHand.type == Material.WRITTEN_BOOK) {
                    val meta = sender.inventory.itemInMainHand.itemMeta as BookMeta
                    sender.inventory.itemInMainHand.type = Material.WRITTEN_BOOK
                    sender.inventory.itemInMainHand.itemMeta = meta
                    sender.sendMessage(plugin.messages["unsign-valid"])
                } else {
                    sender.sendMessage(plugin.messages["unsign-invalid-book"])
                }
            } else {
                sender.sendMessage(plugin.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-unsign"])
        }
        return true
    }

}
