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

package com.rpkit.monsters.bukkit.command.monsterspawnarea

import com.rpkit.core.service.Services
import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnAreaService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player


class MonsterSpawnAreaRemoveMonsterCommand(private val plugin: RPKMonstersBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.monsters.command.monsterspawnarea.removemonster")) {
            sender.sendMessage(plugin.messages["no-permission-monster-spawn-area-remove-monster"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val monsterSpawnAreaService = Services[RPKMonsterSpawnAreaService::class.java]
        if (monsterSpawnAreaService == null) {
            sender.sendMessage(plugin.messages["no-monster-spawn-area-service"])
            return true
        }
        val monsterSpawnArea = monsterSpawnAreaService.getSpawnArea(sender.location)
        if (monsterSpawnArea == null) {
            sender.sendMessage(plugin.messages["monster-spawn-area-remove-monster-invalid-area"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["monster-spawn-area-remove-monster-usage"])
            return true
        }
        val monsterType = try {
            EntityType.valueOf(args[0].toUpperCase())
        } catch (exception: IllegalArgumentException) {
            null
        }
        if (monsterType == null) {
            sender.sendMessage(plugin.messages["monster-spawn-area-remove-monster-invalid-monster-type"])
            return true
        }
        monsterSpawnArea.removeMonster(monsterType)
        sender.sendMessage(plugin.messages["monster-spawn-area-remove-monster-valid"])
        return true
    }
}