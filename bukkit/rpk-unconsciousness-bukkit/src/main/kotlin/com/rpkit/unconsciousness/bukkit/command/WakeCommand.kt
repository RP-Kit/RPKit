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

package com.rpkit.unconsciousness.bukkit.command

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.unconsciousness.bukkit.RPKUnconsciousnessBukkit
import com.rpkit.unconsciousness.bukkit.unconsciousness.RPKUnconsciousnessProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class WakeCommand(private val plugin: RPKUnconsciousnessBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val target = if (args.isNotEmpty()) plugin.server.getPlayer(args[0]) ?: sender as? Player else sender as? Player
        if (target != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val unconsciousnessProvider = plugin.core.serviceManager.getServiceProvider(RPKUnconsciousnessProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(target)
            if (minecraftProfile != null) {
                val character = characterProvider.getActiveCharacter(minecraftProfile)
                if (character != null) {
                    if (unconsciousnessProvider.isUnconscious(character)) {
                        unconsciousnessProvider.setUnconscious(character, false)
                        sender.sendMessage(plugin.messages["wake-success", mapOf(
                                Pair("character", character.name)
                        )])
                    } else {
                        sender.sendMessage(plugin.messages["wake-already-awake", mapOf(
                                Pair("character", character.name)
                        )])
                    }
                } else {
                    sender.sendMessage(plugin.messages["no-character-other", mapOf(
                            Pair("player", minecraftProfile.minecraftUsername)
                    )])
                }
            } else {
                sender.sendMessage(plugin.messages["no-minecraft-profile-other", mapOf(
                        Pair("player", target.name)
                )])
            }
        } else {
            sender.sendMessage(plugin.messages["wake-no-target"])
        }
        return true
    }
}