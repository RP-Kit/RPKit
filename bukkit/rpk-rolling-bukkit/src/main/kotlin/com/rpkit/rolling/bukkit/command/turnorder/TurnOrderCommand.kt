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

package com.rpkit.rolling.bukkit.command.turnorder

import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.CommandResult
import com.rpkit.core.command.result.IncorrectUsageFailure
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.rolling.bukkit.RPKRollingBukkit
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class TurnOrderCommand(private val plugin: RPKRollingBukkit) : RPKCommandExecutor {

    private val turnOrderCreateCommand = TurnOrderCreateCommand(plugin)
    private val turnOrderAddCommand = TurnOrderAddCommand(plugin)
    private val turnOrderRemoveCommand = TurnOrderRemoveCommand(plugin)
    private val turnOrderAdvanceCommand = TurnOrderAdvanceCommand(plugin)
    private val turnOrderShowCommand = TurnOrderShowCommand(plugin)
    private val turnOrderHideCommand = TurnOrderHideCommand(plugin)

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.turnOrderUsage)
            return completedFuture(IncorrectUsageFailure())
        }
        return when (args[0].lowercase()) {
            "create" -> turnOrderCreateCommand.onCommand(sender, args.drop(1).toTypedArray())
            "add" -> turnOrderAddCommand.onCommand(sender, args.drop(1).toTypedArray())
            "remove" -> turnOrderRemoveCommand.onCommand(sender, args.drop(1).toTypedArray())
            "advance" -> turnOrderAdvanceCommand.onCommand(sender, args.drop(1).toTypedArray())
            "show" -> turnOrderShowCommand.onCommand(sender, args.drop(1).toTypedArray())
            "hide" -> turnOrderHideCommand.onCommand(sender, args.drop(1).toTypedArray())
            else -> {
                sender.sendMessage(plugin.messages.turnOrderUsage)
                completedFuture(IncorrectUsageFailure())
            }
        }
    }
}