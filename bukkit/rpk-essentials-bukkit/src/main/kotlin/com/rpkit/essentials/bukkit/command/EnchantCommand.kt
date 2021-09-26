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
import org.bukkit.NamespacedKey
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player

class EnchantCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission("rpkit.essentials.command.enchant")) {
            sender.sendMessage(plugin.messages["no-permission-enchant"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (sender.inventory.itemInMainHand.type == Material.AIR) {
            sender.sendMessage(plugin.messages["enchant-invalid-item"])
            return true
        }
        if (args.size < 2) {
            sender.sendMessage(plugin.messages["enchant-usage"])
            return true
        }
        if (sender.hasPermission("rpkit.essentials.command.enchant.unsafe")) {
            val enchantment = Enchantment.getByKey(NamespacedKey.minecraft(args[0].uppercase()))
            if (enchantment == null) {
                sender.sendMessage(plugin.messages["enchant-invalid-enchantment"])
                return true
            }
            try {
                val level = args[1].toInt()
                sender.inventory.itemInMainHand.addUnsafeEnchantment(enchantment, level)
                sender.sendMessage(plugin.messages["enchant-valid", mapOf(
                        "amount" to sender.inventory.itemInMainHand.amount.toString(),
                        "type" to sender.inventory.itemInMainHand.type.toString().lowercase().replace('_', ' '),
                        "enchantment" to enchantment.key.key,
                        "level" to level.toString()
                )])
            } catch (exception: NumberFormatException) {
                sender.sendMessage(plugin.messages["enchant-invalid-level"])
            }
        } else {
            val enchantment = Enchantment.getByKey(NamespacedKey.minecraft(args[0]))
            if (enchantment == null) {
                sender.sendMessage(plugin.messages["enchant-invalid-enchantment"])
                return true
            }
            try {
                val level = args[1].toInt()
                sender.inventory.itemInMainHand.addEnchantment(enchantment, Integer.parseInt(args[1]))
                sender.sendMessage(plugin.messages["enchant-valid", mapOf(
                        "amount" to sender.inventory.itemInMainHand.amount.toString(),
                        "type" to sender.inventory.itemInMainHand.type.toString().lowercase().replace('_', ' '),
                        "enchantment" to enchantment.key.key,
                        "level" to level.toString()
                )])
            } catch (exception: NumberFormatException) {
                sender.sendMessage(plugin.messages["enchant-invalid-level"])
            } catch (exception: IllegalArgumentException) {
                sender.sendMessage(plugin.messages["enchant-invalid-illegal"])
            }
        }
        return true
    }

}
