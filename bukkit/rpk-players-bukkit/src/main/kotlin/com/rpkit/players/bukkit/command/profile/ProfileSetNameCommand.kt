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
import com.rpkit.players.bukkit.command.result.NoProfileSelfFailure
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileName
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import java.util.concurrent.CompletableFuture


class ProfileSetNameCommand(private val plugin: RPKPlayersBukkit) : RPKCommandExecutor {

    class InvalidNameFailure : CommandFailure()

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<CommandResult> {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.profileSetNameUsage)
            return CompletableFuture.completedFuture(IncorrectUsageFailure())
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
        val name = RPKProfileName(args[0])
        if (!name.value.matches(Regex("[A-z0-9_]{3,16}"))) {
            sender.sendMessage(plugin.messages.profileSetNameInvalidName)
            return CompletableFuture.completedFuture(InvalidNameFailure())
        }
        val profileService = Services[RPKProfileService::class.java]
        if (profileService == null) {
            sender.sendMessage(plugin.messages.noProfileService)
            return CompletableFuture.completedFuture(MissingServiceFailure(RPKProfileService::class.java))
        }
        profile.name = name
        profileService.generateDiscriminatorFor(name).thenAccept { discriminator ->
            profile.discriminator = discriminator
            profileService.updateProfile(profile)
            sender.sendMessage(plugin.messages.profileSetNameValid.withParameters(name = name))
        }
        return CompletableFuture.completedFuture(CommandSuccess)
    }
}