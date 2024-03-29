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

package com.rpkit.players.bukkit.command.profile

import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.*
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class ProfileConfirmLinkCommand(private val plugin: RPKPlayersBukkit) : RPKCommandExecutor {

    class InvalidIdFailure : CommandFailure()
    class AlreadyLinkedFailure(val linkedProfile: RPKProfile) : CommandFailure()
    class InvalidRequestFailure : CommandFailure()
    class InvalidProfileTypeFailure : CommandFailure()

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<CommandResult> {
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return CompletableFuture.completedFuture(NotAPlayerFailure())
        }
        if (args.size <= 1) {
            sender.sendMessage(plugin.messages.profileConfirmLinkUsage)
            return CompletableFuture.completedFuture(IncorrectUsageFailure())
        }
        val type = args[0]
        val id = args[1].toIntOrNull()
        when (type.lowercase()) {
            "minecraft" -> {
                if (id == null) {
                    sender.sendMessage(plugin.messages.profileConfirmLinkInvalidId)
                    return CompletableFuture.completedFuture(InvalidIdFailure())
                }
                val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                if (minecraftProfileService == null) {
                    sender.sendMessage(plugin.messages.noMinecraftProfileService)
                    return CompletableFuture.completedFuture(MissingServiceFailure(RPKMinecraftProfileService::class.java))
                }
                val profile = sender.profile
                if (profile is RPKProfile) {
                    sender.sendMessage(plugin.messages.profileConfirmLinkInvalidAlreadyLinked)
                    return CompletableFuture.completedFuture(AlreadyLinkedFailure(profile))
                }
                return minecraftProfileService.getMinecraftProfileLinkRequests(sender).thenApplyAsync { linkRequests ->
                    val linkRequest = linkRequests.firstOrNull { request -> request.profile.id?.value == id }
                    if (linkRequest == null) {
                        sender.sendMessage(plugin.messages.profileConfirmLinkInvalidRequest)
                        return@thenApplyAsync InvalidRequestFailure()
                    }
                    sender.profile = linkRequest.profile
                    minecraftProfileService.updateMinecraftProfile(sender).join()
                    minecraftProfileService.removeMinecraftProfileLinkRequest(linkRequest).join()
                    minecraftProfileService.loadMinecraftProfile(sender.minecraftUUID).join()
                    sender.sendMessage(plugin.messages.profileConfirmLinkValid)
                    return@thenApplyAsync CommandSuccess
                }.exceptionally { exception ->
                    plugin.logger.log(Level.SEVERE, "Failed to confirm minecraft profile link", exception)
                    throw exception
                }

            }
            else -> {
                sender.sendMessage(plugin.messages.profileConfirmLinkInvalidType)
                return CompletableFuture.completedFuture(InvalidProfileTypeFailure())
            }
        }
    }

}