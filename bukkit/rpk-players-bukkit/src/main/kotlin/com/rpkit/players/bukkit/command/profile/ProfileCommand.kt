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
import com.rpkit.core.command.result.CommandResult
import com.rpkit.core.command.result.IncorrectUsageFailure
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.players.bukkit.RPKPlayersBukkit
import java.util.concurrent.CompletableFuture


class ProfileCommand(private val plugin: RPKPlayersBukkit) : RPKCommandExecutor {

    private val profileViewCommand = ProfileViewCommand(plugin)
    private val profileSetCommand = ProfileSetCommand(plugin)
    private val profileLinkCommand = ProfileLinkCommand(plugin)
    private val profileConfirmLinkCommand = ProfileConfirmLinkCommand(plugin)
    private val profileDenyLinkCommand = ProfileDenyLinkCommand(plugin)

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<CommandResult> {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.profileUsage)
            return CompletableFuture.completedFuture(IncorrectUsageFailure())
        }
        val newArgs = args.drop(1).toTypedArray()
        return when (args[0].toLowerCase()) {
            "view" -> profileViewCommand.onCommand(sender, newArgs)
            "set" -> profileSetCommand.onCommand(sender, newArgs)
            "link" -> profileLinkCommand.onCommand(sender, newArgs)
            "confirmlink" -> profileConfirmLinkCommand.onCommand(sender, newArgs)
            "denylink" -> profileDenyLinkCommand.onCommand(sender, newArgs)
            else -> {
                sender.sendMessage(plugin.messages.profileUsage)
                CompletableFuture.completedFuture(IncorrectUsageFailure())
            }
        }
    }
}