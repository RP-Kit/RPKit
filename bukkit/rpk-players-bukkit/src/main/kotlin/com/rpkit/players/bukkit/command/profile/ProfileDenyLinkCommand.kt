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
import com.rpkit.players.bukkit.profile.RPKProfileName
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class ProfileDenyLinkCommand(private val plugin: RPKPlayersBukkit) : RPKCommandExecutor {

    class InvalidIdFailure : CommandFailure()
    class InvalidRequestFailure : CommandFailure()
    class InvalidAccountTypeFailure : CommandFailure()

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<CommandResult> {
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return CompletableFuture.completedFuture(NotAPlayerFailure())
        }
        if (args.size <= 1) {
            sender.sendMessage(plugin.messages.profileDenyLinkUsage)
            return CompletableFuture.completedFuture(IncorrectUsageFailure())
        }
        val type = args[0]
        val id = args[1].toIntOrNull()
        when (type.lowercase()) {
            "minecraft" -> {
                if (id == null) {
                    sender.sendMessage(plugin.messages.profileDenyLinkInvalidId)
                    return CompletableFuture.completedFuture(InvalidIdFailure())
                }
                val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                if (minecraftProfileService == null) {
                    sender.sendMessage(plugin.messages.noMinecraftProfileService)
                    return CompletableFuture.completedFuture(MissingServiceFailure(RPKMinecraftProfileService::class.java))
                }
                return minecraftProfileService.getMinecraftProfileLinkRequests(sender).thenApplyAsync { immutableLinkRequests ->
                    val linkRequests = immutableLinkRequests.toMutableList()
                    val linkRequest = linkRequests.firstOrNull { request -> request.profile.id?.value == id }
                    if (linkRequest == null) {
                        sender.sendMessage(plugin.messages.profileDenyLinkInvalidRequest)
                        return@thenApplyAsync InvalidRequestFailure()
                    }
                    minecraftProfileService.removeMinecraftProfileLinkRequest(linkRequest)
                    linkRequests.remove(linkRequest)
                    if (linkRequests.isNotEmpty()) {
                        sender.sendMessage(plugin.messages.profileDenyLinkValid)
                        return@thenApplyAsync CommandSuccess
                    }
                    // If they no longer have any link requests pending, we can create a new profile for them based on their
                    // Minecraft profile.
                    val profileService = Services[RPKProfileService::class.java]
                    if (profileService == null) {
                        sender.sendMessage(plugin.messages.noProfileService)
                        return@thenApplyAsync MissingServiceFailure(RPKProfileService::class.java)
                    }
                    return@thenApplyAsync profileService.createProfile(RPKProfileName(sender.name))
                        .thenApply createProfile@{ profile ->
                            sender.profile = profile
                            minecraftProfileService.updateMinecraftProfile(sender)
                            sender.sendMessage(plugin.messages.profileDenyLinkProfileCreated)
                            return@createProfile CommandSuccess
                        }.join()
                }.exceptionally { exception ->
                    plugin.logger.log(Level.SEVERE, "Failed to deny minecraft profile link", exception)
                    throw exception
                }
            }
            else -> {
                sender.sendMessage(plugin.messages.profileDenyLinkInvalidType)
                return CompletableFuture.completedFuture(InvalidAccountTypeFailure())
            }
        }
    }
}