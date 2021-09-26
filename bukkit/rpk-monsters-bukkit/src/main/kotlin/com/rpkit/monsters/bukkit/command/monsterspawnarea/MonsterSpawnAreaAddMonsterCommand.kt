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

class MonsterSpawnAreaAddMonsterCommand(private val plugin: RPKMonstersBukkit) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.monsters.command.monsterspawnarea.addmonster")) {
            sender.sendMessage(plugin.messages["no-permission-monster-spawn-area-add-monster"])
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
            sender.sendMessage(plugin.messages["monster-spawn-area-add-monster-invalid-area"])
            return true
        }
        if (args.size < 3) {
            sender.sendMessage(plugin.messages["monster-spawn-area-add-monster-usage"])
            return true
        }
        val monsterType = try {
            EntityType.valueOf(args[0].uppercase())
        } catch (exception: IllegalArgumentException) {
            null
        }
        if (monsterType == null) {
            sender.sendMessage(plugin.messages["monster-spawn-area-add-monster-invalid-monster-type"])
            return true
        }
        val minLevel = args[1].toIntOrNull()
        if (minLevel == null) {
            sender.sendMessage(plugin.messages["monster-spawn-area-add-monster-invalid-min-level"])
            return true
        }
        val maxLevel = args[2].toIntOrNull()
        if (maxLevel == null) {
            sender.sendMessage(plugin.messages["monster-spawn-area-add-monster-invalid-max-level"])
            return true
        }
        monsterSpawnArea.addMonster(monsterType, minLevel, maxLevel).thenRun {
            sender.sendMessage(plugin.messages["monster-spawn-area-add-monster-valid"])
        }
        return true
    }

}
