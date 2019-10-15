/*
 * Copyright 2019 Ren Binden
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

import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnAreaImpl
import com.rpkit.monsters.bukkit.monsterspawnarea.RPKMonsterSpawnAreaProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.selection.bukkit.selection.RPKSelectionProvider
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class MonsterSpawnAreaCreateCommand(private val plugin: RPKMonstersBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.monsters.command.monsterspawnarea.create")) {
            sender.sendMessage(plugin.messages["no-permission-monster-spawn-area-create"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-self"])
            return true
        }
        val selectionProvider = plugin.core.serviceManager.getServiceProvider(RPKSelectionProvider::class)
        val selection = selectionProvider.getSelection(minecraftProfile)
        val monsterSpawnAreaProvider = plugin.core.serviceManager.getServiceProvider(RPKMonsterSpawnAreaProvider::class)
        monsterSpawnAreaProvider.addSpawnArea(RPKMonsterSpawnAreaImpl(
                plugin,
                minPoint = selection.minimumPoint.location,
                maxPoint = selection.maximumPoint.location
        ))
        sender.sendMessage(plugin.messages["monster-spawn-area-create-valid", mapOf(
                "world" to selection.minimumPoint.world.name,
                "min-x" to selection.minimumPoint.x.toString(),
                "min-y" to selection.minimumPoint.y.toString(),
                "min-z" to selection.minimumPoint.z.toString(),
                "max-x" to selection.maximumPoint.x.toString(),
                "max-y" to selection.maximumPoint.y.toString(),
                "max-z" to selection.maximumPoint.z.toString()
        )])
        return true
    }

}