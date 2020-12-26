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

package com.rpkit.core.bukkit.command

import com.rpkit.core.bukkit.command.sender.resolver.RPKBukkitCommandSenderResolutionService
import com.rpkit.core.command.RPKCommand
import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.service.Services
import org.bukkit.ChatColor.RED
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class RPKBukkitCommand : RPKCommand, CommandExecutor {

    override lateinit var executor: RPKCommandExecutor

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val commandSender = Services[RPKBukkitCommandSenderResolutionService::class.java]?.resolve(sender)
        if (commandSender == null) {
            sender.sendMessage("${RED}Failed to resolve sender.")
            return true
        }
        executor.onCommand(commandSender, args)
        return true
    }
}