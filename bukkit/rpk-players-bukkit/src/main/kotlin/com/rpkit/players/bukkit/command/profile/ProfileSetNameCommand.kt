/*
 * Copyright 2020 Ren Binden
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
import com.rpkit.core.command.result.CommandFailure
import com.rpkit.core.command.result.CommandResult
import com.rpkit.core.command.result.CommandSuccess
import com.rpkit.core.command.result.IncorrectUsageFailure
import com.rpkit.core.command.result.MissingServiceFailure
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.command.result.NoProfileSelfFailure
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile


class ProfileSetNameCommand(private val plugin: RPKPlayersBukkit) : RPKCommandExecutor {

    class InvalidNameFailure : CommandFailure()

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CommandResult {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.profileSetNameUsage)
            return IncorrectUsageFailure()
        }
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return NotAPlayerFailure()
        }
        val profile = sender.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages.noProfileSelf)
            return NoProfileSelfFailure()
        }
        val name = args[0]
        if (!name.matches(Regex("[A-z0-9_]{3,16}"))) {
            sender.sendMessage(plugin.messages.profileSetNameInvalidName)
            return InvalidNameFailure()
        }
        val profileService = Services[RPKProfileService::class.java]
        if (profileService == null) {
            sender.sendMessage(plugin.messages.noProfileService)
            return MissingServiceFailure(RPKProfileService::class.java)
        }
        profile.name = name
        profile.discriminator = profileService.generateDiscriminatorFor(name)
        profileService.updateProfile(profile)
        sender.sendMessage(plugin.messages.profileSetNameValid.withParameters(name = name))
        return CommandSuccess
    }
}