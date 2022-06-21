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

import com.rpkit.chat.bukkit.discord.DiscordButton
import com.rpkit.chat.bukkit.discord.DiscordTextButton
import com.rpkit.chat.bukkit.discord.RPKDiscordService
import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.*
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.command.result.NoProfileSelfFailure
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.discord.RPKDiscordProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.logging.Level.SEVERE

class ProfileLinkDiscordCommand(private val plugin: RPKPlayersBukkit) : RPKCommandExecutor {

    class InvalidDiscordUserTagFailure : CommandFailure()
    class InvalidDiscordUserFailure : CommandFailure()

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return CompletableFuture.completedFuture(NotAPlayerFailure())
        }
        if (!sender.hasPermission("rpkit.players.command.profile.link.discord")) {
            sender.sendMessage(plugin.messages.noPermissionProfileLinkDiscord)
            return CompletableFuture.completedFuture(NoPermissionFailure("rpkit.players.command.profile.link.discord"))
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.profileLinkDiscordUsage)
            return CompletableFuture.completedFuture(IncorrectUsageFailure())
        }
        val discordUserName = args[0]
        val discordService = Services[RPKDiscordService::class.java]
        if (discordService == null) {
            sender.sendMessage(plugin.messages.noDiscordService)
            return CompletableFuture.completedFuture(MissingServiceFailure(RPKDiscordService::class.java))
        }
        val discordId = try {
            discordService.getUserId(discordUserName)
        } catch (exception: IllegalArgumentException) {
            sender.sendMessage(plugin.messages.profileLinkDiscordInvalidUserTag)
            return CompletableFuture.completedFuture(InvalidDiscordUserTagFailure())
        }
        if (discordId == null) {
            sender.sendMessage(plugin.messages.profileLinkDiscordInvalidUser)
            return CompletableFuture.completedFuture(InvalidDiscordUserFailure())
        }
        val profile = sender.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages.noProfileSelf)
            return CompletableFuture.completedFuture(NoProfileSelfFailure())
        }
        val discordProfileService = Services[RPKDiscordProfileService::class.java]
        if (discordProfileService == null) {
            sender.sendMessage(plugin.messages.noDiscordProfileService)
            return CompletableFuture.completedFuture(MissingServiceFailure(RPKDiscordProfileService::class.java))
        }
        return discordProfileService.getDiscordProfile(discordId).thenApply { discordProfile ->
            discordService.sendMessage(
                discordProfile,
                "There was a request to link this account to profile ${profile.name.value}. " +
                        "Do you wish to accept this request?",
                DiscordTextButton(UUID.randomUUID().toString(), DiscordButton.Variant.SUCCESS, "Accept") { event ->
                    discordProfile.profile = profile
                    discordProfileService.updateDiscordProfile(discordProfile).thenRun {
                        event.reply("Account linked.")
                    }
                }
            )
            sender.sendMessage(plugin.messages.profileLinkDiscordValid)
            return@thenApply CommandSuccess
        }.exceptionally { exception ->
            plugin.logger.log(SEVERE, "Failed to send Discord message", exception)
            return@exceptionally CommandSuccess
        }
    }
}