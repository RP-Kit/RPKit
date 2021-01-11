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
import com.rpkit.core.command.result.CommandFailure
import com.rpkit.core.command.result.CommandResult
import com.rpkit.core.command.result.CommandSuccess
import com.rpkit.core.command.result.IncorrectUsageFailure
import com.rpkit.core.command.result.MissingServiceFailure
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.RPKProfileName
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService


class ProfileDenyLinkCommand(private val plugin: RPKPlayersBukkit) : RPKCommandExecutor {

    class InvalidIdFailure : CommandFailure()
    class InvalidRequestFailure : CommandFailure()
    class InvalidAccountTypeFailure : CommandFailure()

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CommandResult {
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return NotAPlayerFailure()
        }
        if (args.size <= 1) {
            sender.sendMessage(plugin.messages.profileDenyLinkUsage)
            return IncorrectUsageFailure()
        }
        val type = args[0]
        val id = args[1].toIntOrNull()
        when (type.toLowerCase()) {
            "minecraft" -> {
                if (id == null) {
                    sender.sendMessage(plugin.messages.profileDenyLinkInvalidId)
                    return InvalidIdFailure()
                }
                val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
                if (minecraftProfileService == null) {
                    sender.sendMessage(plugin.messages.noMinecraftProfileService)
                    return MissingServiceFailure(RPKMinecraftProfileService::class.java)
                }
                val linkRequests = minecraftProfileService.getMinecraftProfileLinkRequests(sender).toMutableList()
                val linkRequest = linkRequests.firstOrNull { request -> request.profile.id?.value == id }
                if (linkRequest == null) {
                    sender.sendMessage(plugin.messages.profileDenyLinkInvalidRequest)
                    return InvalidRequestFailure()
                }
                minecraftProfileService.removeMinecraftProfileLinkRequest(linkRequest)
                linkRequests.remove(linkRequest)
                if (linkRequests.isNotEmpty()) {
                    sender.sendMessage(plugin.messages.profileDenyLinkValid)
                    return CommandSuccess
                }
                // If they no longer have any link requests pending, we can create a new profile for them based on their
                // Minecraft profile.
                val profileService = Services[RPKProfileService::class.java]
                if (profileService == null) {
                    sender.sendMessage(plugin.messages.noProfileService)
                    return MissingServiceFailure(RPKProfileService::class.java)
                }
                val profile = profileService.createProfile(RPKProfileName(sender.name))
                sender.profile = profile
                minecraftProfileService.updateMinecraftProfile(sender)
                sender.sendMessage(plugin.messages.profileDenyLinkProfileCreated)
                return CommandSuccess
            }
            else -> {
                sender.sendMessage(plugin.messages.profileDenyLinkInvalidType)
                return InvalidAccountTypeFailure()
            }
        }
    }
}