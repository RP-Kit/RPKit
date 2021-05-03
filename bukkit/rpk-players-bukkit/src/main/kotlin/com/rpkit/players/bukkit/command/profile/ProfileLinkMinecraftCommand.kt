/*
 * Copyright 2021 Ren Binden
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
import com.rpkit.players.bukkit.command.result.InvalidTargetMinecraftProfileFailure
import com.rpkit.players.bukkit.command.result.NoProfileSelfFailure
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftUsername
import java.util.concurrent.CompletableFuture


class ProfileLinkMinecraftCommand(private val plugin: RPKPlayersBukkit) : RPKCommandExecutor {

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<CommandResult> {
        if (!sender.hasPermission("rpkit.players.command.profile.link.minecraft")) {
            sender.sendMessage(plugin.messages.noPermissionProfileLinkMinecraft)
            return CompletableFuture.completedFuture(NoPermissionFailure("rpkit.players.command.profile.link.minecraft"))
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.profileLinkMinecraftUsage)
            return CompletableFuture.completedFuture(IncorrectUsageFailure())
        }
        val minecraftUsername = RPKMinecraftUsername(args[0])
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileService)
            return CompletableFuture.completedFuture(MissingServiceFailure(RPKMinecraftProfileService::class.java))
        }
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(minecraftUsername)
        if (minecraftProfile != null) {
            sender.sendMessage(plugin.messages.profileLinkMinecraftInvalidMinecraftProfile)
            return CompletableFuture.completedFuture(InvalidTargetMinecraftProfileFailure())
        }
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return CompletableFuture.completedFuture(NotAPlayerFailure())
        }
        val profile = sender.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages.noProfileSelf)
            return CompletableFuture.completedFuture(NoProfileSelfFailure())
        }
        return minecraftProfileService.createMinecraftProfile(minecraftUsername).thenApplyAsync { createdMinecraftProfile ->
            minecraftProfileService.createMinecraftProfileLinkRequest(profile, createdMinecraftProfile).thenApply createLinkRequest@{ linkRequest ->
                sender.sendMessage(plugin.messages.profileLinkMinecraftValid)
                return@createLinkRequest CommandSuccess
            }.join()
        }
    }
}