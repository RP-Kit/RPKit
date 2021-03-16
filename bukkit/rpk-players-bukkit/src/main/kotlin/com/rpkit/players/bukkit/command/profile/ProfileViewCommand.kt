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
import com.rpkit.players.bukkit.command.result.NoProfileOtherFailure
import com.rpkit.players.bukkit.command.result.NoProfileSelfFailure
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftUsername
import java.util.concurrent.CompletableFuture

class ProfileViewCommand(private val plugin: RPKPlayersBukkit) : RPKCommandExecutor {

    class InvalidTargetFailure : CommandFailure()

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<CommandResult> {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileService)
            return CompletableFuture.completedFuture(MissingServiceFailure(RPKMinecraftProfileService::class.java))
        }
        var target: RPKMinecraftProfile? = null
        if (args.isNotEmpty() && sender.hasPermission("rpkit.players.command.profile.view.other")) {
            target = minecraftProfileService.getMinecraftProfile(RPKMinecraftUsername(args[0]))
        }
        if (target == null) {
            if (!sender.hasPermission("rpkit.players.command.profile.view.self")) {
                sender.sendMessage(plugin.messages.noPermissionProfileViewSelf)
                return CompletableFuture.completedFuture(NoPermissionFailure("rpkit.players.command.profile.view.self"))
            }
            if (sender is RPKMinecraftProfile) {
                target = sender
            } else {
                sender.sendMessage(plugin.messages.profileViewInvalidTarget)
                return CompletableFuture.completedFuture(InvalidTargetFailure())
            }
        }
        val profile = target.profile
        if (profile !is RPKProfile) {
            if (sender == target) {
                sender.sendMessage(plugin.messages.noProfileSelf)
                return CompletableFuture.completedFuture(NoProfileSelfFailure())
            } else {
                sender.sendMessage(plugin.messages.noProfileOther)
                return CompletableFuture.completedFuture(NoProfileOtherFailure())
            }
        }
        sender.sendMessage(
            plugin.messages.profileViewValid.withParameters(
                name = profile.name,
                discriminator = profile.discriminator
            )
        )
        return CompletableFuture.completedFuture(CommandSuccess)
    }
}