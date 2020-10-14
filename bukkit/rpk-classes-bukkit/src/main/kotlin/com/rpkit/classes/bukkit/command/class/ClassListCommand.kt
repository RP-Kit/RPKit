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

import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.classes.bukkit.classes.RPKClassService
import com.rpkit.core.service.Services
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender


class ClassListCommand(private val plugin: RPKClassesBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.classes.command.class.list")) {
            sender.sendMessage(plugin.messages["no-permission-class-list"])
            return true
        }
        val classService = Services[RPKClassService::class]
        if (classService == null) {
            sender.sendMessage(plugin.messages["no-class-service"])
            return true
        }
        sender.sendMessage(plugin.messages["class-list-title"])
        for (`class` in classService.classes) {
            sender.sendMessage(plugin.messages["class-list-item", mapOf(Pair("class", `class`.name))])
        }
        return true
    }

}