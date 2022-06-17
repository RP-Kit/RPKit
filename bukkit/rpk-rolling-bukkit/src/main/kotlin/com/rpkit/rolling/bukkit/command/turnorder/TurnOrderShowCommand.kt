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
import com.rpkit.players.bukkit.command.result.InvalidTargetMinecraftProfileFailure
import com.rpkit.players.bukkit.command.result.NoMinecraftProfileOtherFailure
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.rolling.bukkit.RPKRollingBukkit
import com.rpkit.rolling.bukkit.command.result.InvalidTurnOrderFailure
import com.rpkit.rolling.bukkit.turnorder.RPKTurnOrderService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class TurnOrderShowCommand(private val plugin: RPKRollingBukkit) : RPKCommandExecutor {
    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (!sender.hasPermission("rpkit.rolling.command.turnorder.show")) {
            sender.sendMessage(plugin.messages.noPermissionTurnOrderShow)
            return completedFuture(NoPermissionFailure("rpkit.rolling.command.turnorder.show"))
        }
        if (args.size < 2) {
            sender.sendMessage(plugin.messages.turnOrderShowUsage)
            return completedFuture(IncorrectUsageFailure())
        }
        val turnOrderService = Services[RPKTurnOrderService::class.java]
        if (turnOrderService == null) {
            sender.sendMessage(plugin.messages.noTurnOrderService)
            return completedFuture(MissingServiceFailure(RPKTurnOrderService::class.java))
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileService)
            return completedFuture(MissingServiceFailure(RPKMinecraftProfileService::class.java))
        }
        val turnOrder = turnOrderService.getTurnOrder(args[0])
        if (turnOrder == null) {
            sender.sendMessage(plugin.messages.turnOrderShowInvalidTurnOrder)
            return completedFuture(InvalidTurnOrderFailure())
        }
        val target = plugin.server.getPlayer(args[1])
        if (target == null) {
            sender.sendMessage(plugin.messages.turnOrderShowInvalidTarget)
            return completedFuture(InvalidTargetMinecraftProfileFailure())
        }
        val targetMinecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(target)
        if (targetMinecraftProfile == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileOther)
            return completedFuture(NoMinecraftProfileOtherFailure())
        }
        turnOrder.show(targetMinecraftProfile)
        sender.sendMessage(plugin.messages.turnOrderShowValid)
        return completedFuture(CommandSuccess)
    }

}
