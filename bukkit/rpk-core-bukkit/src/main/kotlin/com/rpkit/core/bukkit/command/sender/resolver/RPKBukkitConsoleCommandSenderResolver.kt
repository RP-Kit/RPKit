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

package com.rpkit.core.bukkit.command.sender.resolver

import com.rpkit.core.bukkit.command.sender.RPKBukkitConsoleCommandSender
import com.rpkit.core.command.sender.RPKCommandSender
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender

class RPKBukkitConsoleCommandSenderResolver : RPKBukkitCommandSenderResolver {

    override val priority = 0

    override fun resolve(bukkitCommandSender: CommandSender): RPKCommandSender? {
        if (bukkitCommandSender is ConsoleCommandSender) {
            return RPKBukkitConsoleCommandSender(bukkitCommandSender)
        }
        return null
    }

}