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
import com.rpkit.core.command.result.*
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.rolling.bukkit.RPKRollingBukkit
import com.rpkit.rolling.bukkit.command.result.InvalidTurnOrderFailure
import com.rpkit.rolling.bukkit.turnorder.RPKTurnOrderService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class TurnOrderAddCommand(private val plugin: RPKRollingBukkit) : RPKCommandExecutor {
    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (!sender.hasPermission("rpkit.rolling.command.turnorder.add")) {
            sender.sendMessage(plugin.messages.noPermissionTurnOrderAdd)
            return completedFuture(NoPermissionFailure("rpkit.rolling.command.turnorder.add"))
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.turnOrderAddUsage)
            return completedFuture(IncorrectUsageFailure())
        }
        val turnOrderService = Services[RPKTurnOrderService::class.java]
        if (turnOrderService == null) {
            sender.sendMessage(plugin.messages.noTurnOrderService)
            return completedFuture(MissingServiceFailure(RPKTurnOrderService::class.java))
        }
        var argOffset = 0
        var turnOrder = if (args.size > 1) {
            turnOrderService.getTurnOrder(args[0])
        } else null
        if (turnOrder == null && sender is RPKMinecraftProfile) {
            turnOrder = turnOrderService.getActiveTurnOrder(sender)
        } else {
            argOffset = 1
        }
        if (turnOrder == null) {
            sender.sendMessage(plugin.messages.turnOrderAddInvalidTurnOrder)
            return completedFuture(InvalidTurnOrderFailure())
        }
        args.drop(argOffset).forEach { turnOrder.add(it) }
        sender.sendMessage(plugin.messages.turnOrderAddValid)
        return completedFuture(CommandSuccess)
    }
}