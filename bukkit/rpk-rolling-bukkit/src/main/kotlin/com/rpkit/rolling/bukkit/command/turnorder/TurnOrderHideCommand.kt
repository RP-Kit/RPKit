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
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.rolling.bukkit.RPKRollingBukkit
import com.rpkit.rolling.bukkit.turnorder.RPKTurnOrderService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class TurnOrderHideCommand(private val plugin: RPKRollingBukkit) : RPKCommandExecutor {
    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (!sender.hasPermission("rpkit.rolling.command.turnorder.hide")) {
            sender.sendMessage(plugin.messages.noPermissionTurnOrderHide)
            return completedFuture(NoPermissionFailure("rpkit.rolling.command.turnorder.hide"))
        }
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return completedFuture(NotAPlayerFailure())
        }
        val turnOrderService = Services[RPKTurnOrderService::class.java]
        if (turnOrderService == null) {
            sender.sendMessage(plugin.messages.noTurnOrderService)
            return completedFuture(MissingServiceFailure(RPKTurnOrderService::class.java))
        }
        turnOrderService.hideTurnOrder(sender)
        sender.sendMessage(plugin.messages.turnOrderHideValid)
        return completedFuture(CommandSuccess)
    }
}
