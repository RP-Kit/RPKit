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

package com.rpkit.essentials.bukkit.command.issue

import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.*
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.players.bukkit.command.result.NoProfileSelfFailure
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.github.RPKGitHubService
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.github.RPKGitHubProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import org.bukkit.inventory.meta.BookMeta
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class IssueSubmitCommand(private val plugin: RPKEssentialsBukkit) : RPKCommandExecutor {
    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (!sender.hasPermission("rpkit.essentials.command.issue.submit")) {
            sender.sendMessage(plugin.messages.noPermissionIssueSubmit)
            return completedFuture(NoPermissionFailure("rpkit.essentials.command.issue.submit"))
        }
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return completedFuture(NotAPlayerFailure())
        }
        val githubProfileService = Services[RPKGitHubProfileService::class.java]
        if (githubProfileService == null) {
            sender.sendMessage(plugin.messages.noGithubProfileService)
            return completedFuture(MissingServiceFailure(RPKGitHubProfileService::class.java))
        }
        val profile = sender.profile as? RPKProfile
        if (profile == null) {
            sender.sendMessage(plugin.messages.noProfileSelf)
            return completedFuture(NoProfileSelfFailure())
        }
        val githubService = Services[RPKGitHubService::class.java]
        if (githubService == null) {
            sender.sendMessage(plugin.messages.noGithubService)
            return completedFuture(MissingServiceFailure(RPKGitHubService::class.java))
        }
        val bukkitPlayer = plugin.server.getPlayer(sender.minecraftUUID)
        if (bukkitPlayer == null) {
            // no message, player sent a command so should be online
            return completedFuture(PlayerOfflineFailure())
        }
        val itemInHand = bukkitPlayer.inventory.itemInMainHand
        if (!itemInHand.hasItemMeta()) {
            sender.sendMessage(plugin.messages.issueSubmitInvalidBook)
            return completedFuture(NoBookFailure())
        }
        val meta = itemInHand.itemMeta
        if (meta !is BookMeta) {
            sender.sendMessage(plugin.messages.issueSubmitInvalidBook)
            return completedFuture(NoBookFailure())
        }
        val title = meta.title
        val body = meta.pages.joinToString("\n\n")
        if (title == null) {
            sender.sendMessage(plugin.messages.issueSubmitInvalidTitle)
            return completedFuture(NoTitleFailure())
        }
        return githubProfileService.getGitHubProfiles(profile).thenApplyAsync { githubProfiles ->
            val githubProfile = if (args.isNotEmpty()) {
                githubProfiles.firstOrNull { it.name.value == args[0] } ?: githubProfiles.firstOrNull()
            } else {
                githubProfiles.firstOrNull()
            }
            if (githubProfile == null) {
                sender.sendMessage(plugin.messages.noGithubProfile)
                return@thenApplyAsync NoGitHubProfileFailure()
            }
            return@thenApplyAsync githubService.submitIssue(githubProfile, title, body).thenApply { issue ->
                sender.sendMessage(plugin.messages.issueSubmitValid.withParameters(link = issue.link))
                CommandSuccess
            }.join()
        }
    }

    class PlayerOfflineFailure : CommandFailure()
    class NoBookFailure : CommandFailure()
    class NoTitleFailure : CommandFailure()
    class NoGitHubProfileFailure : CommandFailure()
}