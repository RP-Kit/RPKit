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

package com.rpkit.travel.bukkit.command

import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.CommandResult
import com.rpkit.core.command.result.CommandSuccess
import com.rpkit.core.command.result.MissingServiceFailure
import com.rpkit.core.command.result.NoPermissionFailure
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.command.result.NoMinecraftProfileSelfFailure
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.travel.bukkit.untamer.RPKUntamerService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.CompletableFuture.supplyAsync

class UntameCommand(private val plugin: RPKTravelBukkit) : RPKCommandExecutor {
    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (!sender.hasPermission("rpkit.travel.command.untame")) {
            sender.sendMessage(plugin.messages.noPermissionUntame)
            return completedFuture(NoPermissionFailure("rpkit.travel.command.untame"))
        }
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return completedFuture(NotAPlayerFailure())
        }
        val untamerService = Services[RPKUntamerService::class.java]
        if (untamerService == null) {
            sender.sendMessage(plugin.messages.noUntamerService)
            return completedFuture(MissingServiceFailure(RPKUntamerService::class.java))
        }
        val minecraftProfileId = sender.id
        if (minecraftProfileId == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfile)
            return completedFuture(NoMinecraftProfileSelfFailure())
        }
        return supplyAsync {
            untamerService.setUntaming(minecraftProfileId, true).thenRun {
                sender.sendMessage(plugin.messages.untameSelectCreatureToUntame)
            }
            CommandSuccess
        }
    }
}