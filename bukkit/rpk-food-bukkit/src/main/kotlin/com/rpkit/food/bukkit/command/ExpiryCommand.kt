/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.food.bukkit.command

import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.CommandResult
import com.rpkit.core.command.result.IncorrectUsageFailure
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.food.bukkit.RPKFoodBukkit
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class ExpiryCommand(private val plugin: RPKFoodBukkit) : RPKCommandExecutor {

    private val expiryViewCommand = ExpiryViewCommand(plugin)
    private val expirySetCommand = ExpirySetCommand(plugin)

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.expiryUsage)
            return completedFuture(IncorrectUsageFailure())
        }
        return when (args[0].lowercase()) {
            "view", "check", "show", "get" -> expiryViewCommand.onCommand(sender, args.drop(1).toTypedArray())
            "set" -> expirySetCommand.onCommand(sender, args.drop(1).toTypedArray())
            else -> {
                sender.sendMessage(plugin.messages.expiryUsage)
                return completedFuture(IncorrectUsageFailure())
            }
        }
    }
}