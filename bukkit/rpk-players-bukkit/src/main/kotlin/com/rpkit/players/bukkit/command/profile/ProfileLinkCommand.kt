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
import com.rpkit.core.command.result.CommandResult
import com.rpkit.core.command.result.IncorrectUsageFailure
import com.rpkit.core.command.result.NoPermissionFailure
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.players.bukkit.RPKPlayersBukkit
import java.util.concurrent.CompletableFuture

/**
 * Account link command.
 * Links another account to the current player.
 */
class ProfileLinkCommand(private val plugin: RPKPlayersBukkit) : RPKCommandExecutor {

    private val profileLinkIRCCommand = ProfileLinkIRCCommand(plugin)
    private val profileLinkMinecraftCommand = ProfileLinkMinecraftCommand(plugin)
    private val profileLinkDiscordCommand = ProfileLinkDiscordCommand(plugin)
    private val profileLinkGithubCommand = ProfileLinkGithubCommand(plugin)

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (!sender.hasPermission("rpkit.players.command.profile.link")) {
            sender.sendMessage(plugin.messages.noPermissionProfileLink)
            return CompletableFuture.completedFuture(NoPermissionFailure("rpkit.players.command.profile.link"))
        }
        if (args.isNotEmpty()) {
            val newArgs = args.drop(1).toTypedArray()
            return when {
                args[0].equals("irc", ignoreCase = true) ->
                    profileLinkIRCCommand.onCommand(sender, newArgs)
                args[0].equals("minecraft", ignoreCase = true) || args[0].equals("mc", ignoreCase = true) ->
                    profileLinkMinecraftCommand.onCommand(sender, newArgs)
                args[0].equals("discord", ignoreCase = true) ->
                    profileLinkDiscordCommand.onCommand(sender, newArgs)
                args[0].equals("github", ignoreCase = true) ->
                    profileLinkGithubCommand.onCommand(sender, newArgs)
                else -> {
                    sender.sendMessage(plugin.messages.profileLinkUsage)
                    CompletableFuture.completedFuture(IncorrectUsageFailure())
                }
            }
        } else {
            sender.sendMessage(plugin.messages.profileLinkUsage)
            return CompletableFuture.completedFuture(IncorrectUsageFailure())
        }
    }

}
