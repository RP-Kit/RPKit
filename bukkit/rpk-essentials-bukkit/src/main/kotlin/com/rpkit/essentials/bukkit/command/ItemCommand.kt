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
import org.bukkit.inventory.ItemStack

class ItemCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender.hasPermission("rpkit.essentials.command.item")) {
            if (sender is Player) {
                if (args.isNotEmpty()) {
                    val material = Material.matchMaterial(args[0])
                    var amount = 1
                    if (args.size >= 2) {
                        try {
                            amount = Integer.parseInt(args[1])
                        } catch (exception: NumberFormatException) {
                            sender.sendMessage(plugin.messages["item-invalid-amount"])
                            return true
                        }
                    }
                    if (material != null) {
                        val item = ItemStack(material, amount)
                        sender.inventory.addItem(item)
                        if (amount > 1) {
                            sender.sendMessage(plugin.messages["item-valid-plural", mapOf(
                                "type" to material.toString(),
                                "amount" to amount.toString()
                            )])
                        } else {
                            sender.sendMessage(plugin.messages["item-valid-singular", mapOf(
                                "type" to material.toString(),
                                "amount" to amount.toString()
                            )])
                        }
                    } else {
                        sender.sendMessage(plugin.messages["item-invalid-material"])
                    }
                } else {
                    sender.sendMessage(plugin.messages["item-usage"])
                }
            } else {
                sender.sendMessage(plugin.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-item"])
        }
        return true
    }

}
