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

import com.rpkit.core.command.sender.RPKBlockCommandSender
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.command.BlockCommandSender

class RPKBukkitBlockCommandSender(
    private val commandSender: BlockCommandSender
) : RPKBukkitCommandSender, RPKBlockCommandSender {

    override val name: String
        get() = commandSender.name

    override fun sendMessage(vararg components: BaseComponent) {
        commandSender.spigot().sendMessage(*components)
    }

    override fun sendMessage(message: String) {
        commandSender.sendMessage(message)
    }

    override fun sendMessage(messages: Array<String>) {
        commandSender.sendMessage(*messages)
    }

    override fun hasPermission(permission: String): Boolean {
        return commandSender.hasPermission(permission)
    }

}