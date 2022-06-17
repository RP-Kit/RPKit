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
import com.rpkit.core.command.result.CommandSuccess
import com.rpkit.core.command.result.MissingServiceFailure
import com.rpkit.core.command.result.NoPermissionFailure
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.rolling.bukkit.RPKRollingBukkit
import com.rpkit.rolling.bukkit.command.result.InvalidTurnOrderFailure
import com.rpkit.rolling.bukkit.turnorder.RPKTurnOrderService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class TurnOrderAdvanceCommand(private val plugin: RPKRollingBukkit) : RPKCommandExecutor {
    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (!sender.hasPermission("rpkit.rolling.command.turnorder.advance")) {
            sender.sendMessage(plugin.messages.noPermissionTurnOrderAdvance)
            return completedFuture(NoPermissionFailure("rpkit.rolling.command.turnorder.advance"))
        }
        val turnOrderService = Services[RPKTurnOrderService::class.java]
        if (turnOrderService == null) {
            sender.sendMessage(plugin.messages.noTurnOrderService)
            return completedFuture(MissingServiceFailure(RPKTurnOrderService::class.java))
        }
        var turnOrder = if (args.isNotEmpty()) turnOrderService.getTurnOrder(args[0]) else null
        if (turnOrder == null && sender is RPKMinecraftProfile) {
            turnOrder = turnOrderService.getActiveTurnOrder(sender)
        }
        if (turnOrder == null) {
            sender.sendMessage(plugin.messages.turnOrderAdvanceInvalidTurnOrder)
            return completedFuture(InvalidTurnOrderFailure())
        }
        turnOrder.advance()
        sender.sendMessage(plugin.messages.turnOrderAdvanceValid)
        return completedFuture(CommandSuccess)
    }
}