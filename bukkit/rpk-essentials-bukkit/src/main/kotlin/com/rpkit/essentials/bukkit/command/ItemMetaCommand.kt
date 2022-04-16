/*
 * Copyright 2022 Ren Binden
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
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ItemMetaCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission("rpkit.essentials.command.itemmeta")) {
            sender.sendMessage(plugin.messages.noPermissionItemMeta)
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return true
        }
        val itemInMainHand = sender.inventory.itemInMainHand
        if (itemInMainHand.type == Material.AIR) {
            sender.sendMessage(plugin.messages.itemMetaInvalidItem)
            return true
        }
        if (args.size < 2) {
            sender.sendMessage(plugin.messages.itemMetaUsage)
            return true
        }
        val meta = itemInMainHand.itemMeta ?: plugin.server.itemFactory.getItemMeta(itemInMainHand.type)
        if (meta == null) {
            sender.sendMessage(plugin.messages.itemMetaFailedToCreate)
            return true
        }
        when (args[0].lowercase()) {
            "setname" -> {
                val name = ChatColor.translateAlternateColorCodes('&', args.drop(1).joinToString(" "))
                meta.setDisplayName(name)
                sender.sendMessage(plugin.messages.itemMetaSetNameValid.withParameters(
                    name = name
                ))
            }
            "addlore" -> {
                val lore = meta.lore ?: mutableListOf<String>()
                val loreItem = ChatColor.translateAlternateColorCodes('&', args.drop(1).joinToString(" "))
                lore.add(loreItem)
                sender.sendMessage(plugin.messages.itemMetaAddLoreValid.withParameters(
                    lore = loreItem
                ))
                meta.lore = lore
            }
            "removelore" -> {
                if (!meta.hasLore()) {
                    sender.sendMessage(plugin.messages.itemMetaRemoveLoreInvalidLore)
                    return true
                }
                val lore = meta.lore ?: mutableListOf<String>()
                val loreItem = ChatColor.translateAlternateColorCodes('&', args.drop(1).joinToString(" "))
                if (!lore.contains(loreItem)) {
                    sender.sendMessage(plugin.messages.itemMetaRemoveLoreInvalidLoreItem)
                    return true
                }
                lore.remove(loreItem)
                sender.sendMessage(plugin.messages.itemMetaRemoveLoreValid.withParameters(
                    lore = loreItem
                ))
                meta.lore = lore
            }
            "custommodeldata" -> {
                val customModelData = args[1].toIntOrNull()
                if (customModelData == null) {
                    sender.sendMessage(plugin.messages.itemMetaCustomModelDataInvalidCustomModelData)
                    return true
                }
                meta.setCustomModelData(customModelData)
                sender.sendMessage(plugin.messages.itemMetaCustomModelDataValid.withParameters(
                    customModelData = customModelData
                ))
            }
            else -> {
                sender.sendMessage(plugin.messages.itemMetaUsage)
            }
        }
        itemInMainHand.itemMeta = meta
        return true
    }

}
