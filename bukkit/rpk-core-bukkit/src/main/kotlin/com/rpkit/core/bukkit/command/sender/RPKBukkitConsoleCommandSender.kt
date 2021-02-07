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

package com.rpkit.core.bukkit.command.sender

import com.rpkit.core.command.sender.RPKConsoleCommandSender
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.command.ConsoleCommandSender

class RPKBukkitConsoleCommandSender(
    private val bukkitCommandSender: ConsoleCommandSender
) : RPKBukkitCommandSender, RPKConsoleCommandSender {

    override val name: String
        get() = bukkitCommandSender.name

    override fun hasPermission(permission: String): Boolean {
        return bukkitCommandSender.hasPermission(permission)
    }

    override fun sendMessage(message: String) {
        bukkitCommandSender.sendMessage(message)
    }

    override fun sendMessage(messages: Array<String>) {
        bukkitCommandSender.sendMessage(messages)
    }

    override fun sendMessage(vararg components: BaseComponent) {
        bukkitCommandSender.spigot().sendMessage(*components)
    }

}