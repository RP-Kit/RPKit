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

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.bukkit.extension.tagToNbtJson
import com.rpkit.core.service.Services
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_ITEM
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Material.AIR
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class ShowItemCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.essentials.command.showitem")) {
            sender.sendMessage(plugin.messages.noPermissionShowItem)
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.showItemUsage)
            return true
        }
        val target = plugin.server.getPlayer(args[0])
        if (target == null) {
            sender.sendMessage(plugin.messages.showItemInvalidTarget)
            return true
        }
        val itemInHand = sender.inventory.itemInMainHand
        if (itemInHand.type == AIR) {
            sender.sendMessage(plugin.messages.showItemInvalidNoItem)
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileService)
            return true
        }
        minecraftProfileService.getMinecraftProfile(sender).thenAccept getMinecraftProfile@{ minecraftProfile ->
            if (minecraftProfile == null) {
                sender.sendMessage(plugin.messages.noMinecraftProfile)
                return@getMinecraftProfile
            }
            val characterService = Services[RPKCharacterService::class.java]
            if (characterService == null) {
                sender.sendMessage(plugin.messages.noCharacterService)
                return@getMinecraftProfile
            }
            characterService.getActiveCharacter(minecraftProfile).thenAccept getActiveCharacter@{ character ->
                if (character == null) {
                    sender.sendMessage(plugin.messages.noCharacterSelf)
                    return@getActiveCharacter
                }
                target.spigot().sendMessage(
                    *ComponentBuilder("")
                        .append(
                            TextComponent(plugin.messages.showItemValid.withParameters(
                                character = character,
                                item = itemInHand
                            )).apply {
                                hoverEvent = HoverEvent(
                                    SHOW_ITEM,
                                    ComponentBuilder(itemInHand.tagToNbtJson()).create()
                                )
                            }
                        )
                        .create()
                )
            }
        }
        return true
    }

}