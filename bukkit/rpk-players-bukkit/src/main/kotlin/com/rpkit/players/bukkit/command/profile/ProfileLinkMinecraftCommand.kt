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
import com.rpkit.core.command.result.CommandResult
import com.rpkit.core.command.result.CommandSuccess
import com.rpkit.core.command.result.IncorrectUsageFailure
import com.rpkit.core.command.result.MissingServiceFailure
import com.rpkit.core.command.result.NoPermissionFailure
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


class ProfileLinkMinecraftCommand(private val plugin: RPKPlayersBukkit) : RPKCommandExecutor {

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CommandResult {
        if (!sender.hasPermission("rpkit.players.command.profile.link.minecraft")) {
            sender.sendMessage(plugin.messages.noPermissionProfileLinkMinecraft)
            return NoPermissionFailure("rpkit.players.command.profile.link.minecraft")
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.profileLinkMinecraftUsage)
            return IncorrectUsageFailure()
        }
        val minecraftUsername = RPKMinecraftUsername(args[0])
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages.noMinecraftProfileService)
            return MissingServiceFailure(RPKMinecraftProfileService::class.java)
        }
        var minecraftProfile = minecraftProfileService.getMinecraftProfile(minecraftUsername)
        if (minecraftProfile != null) {
            sender.sendMessage(plugin.messages.profileLinkMinecraftInvalidMinecraftProfile)
            return InvalidTargetMinecraftProfileFailure()
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
        minecraftProfile = minecraftProfileService.createMinecraftProfile(minecraftUsername)
        minecraftProfileService.createMinecraftProfileLinkRequest(profile, minecraftProfile)
        sender.sendMessage(plugin.messages.profileLinkMinecraftValid)
        return CommandSuccess
    }
}