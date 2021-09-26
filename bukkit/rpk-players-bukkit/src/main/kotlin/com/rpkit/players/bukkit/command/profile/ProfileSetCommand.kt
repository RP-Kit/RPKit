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

package com.rpkit.players.bukkit.command.profile

import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.CommandResult
import com.rpkit.core.command.result.IncorrectUsageFailure
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.players.bukkit.RPKPlayersBukkit
import java.util.concurrent.CompletableFuture

class ProfileSetCommand(private val plugin: RPKPlayersBukkit) : RPKCommandExecutor {

    private val profileSetNameCommand = ProfileSetNameCommand(plugin)
    private val profileSetPasswordCommand = ProfileSetPasswordCommand(plugin)

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<CommandResult> {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.profileSetUsage)
            return CompletableFuture.completedFuture(IncorrectUsageFailure())
        }
        val newArgs = args.drop(1).toTypedArray()
        return when (args[0].lowercase()) {
            "name" -> profileSetNameCommand.onCommand(sender, newArgs)
            "password" -> profileSetPasswordCommand.onCommand(sender, newArgs)
            else -> {
                sender.sendMessage(plugin.messages.profileSetUsage)
                CompletableFuture.completedFuture(IncorrectUsageFailure())
            }
        }
    }
}