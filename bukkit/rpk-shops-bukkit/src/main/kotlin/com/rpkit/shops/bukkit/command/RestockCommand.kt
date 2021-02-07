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

package com.rpkit.shops.bukkit.command

import com.rpkit.shops.bukkit.RPKShopsBukkit
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack


class RestockCommand(private val plugin: RPKShopsBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.shops.command.restock")) {
            if (sender is Player) {
                val transparent: Set<Material>? = null
                val targetBlock = sender.getTargetBlock(transparent, 8)
                val chest = targetBlock.state
                if (chest is Chest) {
                    if (args.isNotEmpty()) {
                        val material = Material.matchMaterial(args.joinToString(" "))
                        if (material != null) {
                            for (i in 0 until chest.inventory.size) {
                                chest.inventory.setItem(i, ItemStack(material, material.maxStackSize))
                            }
                            sender.sendMessage(plugin.messages["restock-valid"])
                        } else {
                            sender.sendMessage(plugin.messages["restock-invalid-material"])
                        }
                    } else {
                        sender.sendMessage(plugin.messages["restock-usage"])
                    }
                } else {
                    sender.sendMessage(plugin.messages["restock-invalid-chest"])
                }
            } else {
                sender.sendMessage(plugin.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-restock"])
        }
        return true
    }
}