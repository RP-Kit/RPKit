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
import com.rpkit.core.command.result.*
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.food.bukkit.RPKFoodBukkit
import com.rpkit.food.bukkit.expiry.RPKExpiryService
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import java.time.Duration
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class ExpiryViewCommand(private val plugin: RPKFoodBukkit) : RPKCommandExecutor {
    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return completedFuture(NotAPlayerFailure())
        }
        if (!sender.hasPermission("rpkit.food.command.expiry.view")) {
            sender.sendMessage(plugin.messages.noPermissionExpiryView)
            return completedFuture(NoPermissionFailure("rpkit.food.command.expiry.view"))
        }
        val expiryService = Services[RPKExpiryService::class.java]
        if (expiryService == null) {
            sender.sendMessage(plugin.messages.noExpiryService)
            return completedFuture(MissingServiceFailure(RPKExpiryService::class.java))
        }
        val bukkitPlayer = plugin.server.getPlayer(sender.minecraftUUID)
        if (bukkitPlayer == null) {
            // No message here as if they are sending a command as a Minecraft profile they are online already.
            return completedFuture(NotAPlayerFailure())
        }
        val itemInHand = bukkitPlayer.inventory.itemInMainHand
        val expiry = expiryService.getExpiry(itemInHand)
        if (expiry != null) {
            val duration = Duration.between(ZonedDateTime.now(), expiry)
            if (duration.isNegative) {
                sender.sendMessage(plugin.messages.expiryViewInvalidExpired)
                return completedFuture(ExpiredFailure())
            }
            sender.sendMessage(plugin.messages.expiryViewValid.withParameters(
                duration = duration
            ))
            return completedFuture(CommandSuccess)
        } else {
            sender.sendMessage(plugin.messages.expiryViewInvalidNoExpiry)
            return completedFuture(NoExpiryFailure())
        }
    }

    class NoExpiryFailure : CommandFailure()
    class ExpiredFailure : CommandFailure()
}