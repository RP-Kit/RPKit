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
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnAreaImpl
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnAreaService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.selection.bukkit.selection.RPKSelectionService
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class MonsterSpawnAreaCreateCommand(private val plugin: RPKMonstersBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.monsters.command.monsterspawnarea.create")) {
            sender.sendMessage(plugin.messages.noPermissionMonsterSpawnAreaCreate)
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileService)
            return true
        }
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileSelf)
            return true
        }
        val selectionService = Services[RPKSelectionService::class.java]
        if (selectionService == null) {
            sender.sendMessage(plugin.messages.noSelectionService)
            return true
        }
        selectionService.getSelection(minecraftProfile).thenAccept { selection ->
            if (selection == null) {
                sender.sendMessage(plugin.messages.noSelection)
                return@thenAccept
            }
            val monsterSpawnAreaService = Services[RPKMonsterSpawnAreaService::class.java]
            if (monsterSpawnAreaService == null) {
                sender.sendMessage(plugin.messages.noMonsterSpawnAreaService)
                return@thenAccept
            }
            monsterSpawnAreaService.addSpawnArea(
                RPKMonsterSpawnAreaImpl(
                    plugin,
                    minPoint = selection.minimumPoint,
                    maxPoint = selection.maximumPoint
                )
            ).thenRun {
                sender.sendMessage(
                    plugin.messages.monsterSpawnAreaCreateValid.withParameters(
                        world = Bukkit.getWorld(selection.world) ?: Bukkit.getWorlds()[0],
                        selection.minimumPoint.x,
                        selection.minimumPoint.y,
                        selection.minimumPoint.z,
                        selection.maximumPoint.x,
                        selection.maximumPoint.y,
                        selection.maximumPoint.z
                    )
                )
            }
        }
        return true
    }

}