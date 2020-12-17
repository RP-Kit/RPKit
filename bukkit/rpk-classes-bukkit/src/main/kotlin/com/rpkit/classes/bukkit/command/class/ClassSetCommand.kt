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

package com.rpkit.classes.bukkit.command.`class`

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.classes.bukkit.classes.RPKClassService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class ClassSetCommand(private val plugin: RPKClassesBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.classes.command.class.set")) {
            sender.sendMessage(plugin.messages["no-permission-class-set"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["class-set-usage"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }
        val classService = Services[RPKClassService::class.java]
        if (classService == null) {
            sender.sendMessage(plugin.messages["no-class-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val character = characterService.getActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages["no-character"])
            return true
        }
        val className = args[0]
        val `class` = classService.getClass(className)
        if (`class` == null) {
            sender.sendMessage(plugin.messages["class-set-invalid-class"])
            return true
        }
        if (!`class`.hasPrerequisites(character)) {
            sender.sendMessage(plugin.messages["class-set-invalid-prerequisites"])
            return true
        }
        classService.setClass(character, `class`)
        sender.sendMessage(plugin.messages["class-set-valid", mapOf(
                Pair("class", `class`.name)
        )])
        return true
    }
}