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
import com.rpkit.core.command.result.*
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.command.result.NoProfileSelfFailure
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.github.RPKGitHubProfileService
import com.rpkit.players.bukkit.profile.github.RPKGitHubUsername
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import org.kohsuke.github.GitHub
import java.io.IOException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.CompletableFuture.supplyAsync

class ProfileLinkGithubCommand(private val plugin: RPKPlayersBukkit) : RPKCommandExecutor {
    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (!sender.hasPermission("rpkit.players.command.profile.link.github")) {
            sender.sendMessage(plugin.messages.noPermissionProfileLinkGithub)
            return completedFuture(NoPermissionFailure("rpkit.players.command.profile.link.github"))
        }
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return completedFuture(NotAPlayerFailure())
        }
        val profile = sender.profile as? RPKProfile
        if (profile == null) {
            sender.sendMessage(plugin.messages.noProfileSelf)
            return completedFuture(NoProfileSelfFailure())
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.profileLinkGithubUsage)
            return completedFuture(IncorrectUsageFailure())
        }
        val githubProfileService = Services[RPKGitHubProfileService::class.java]
        if (githubProfileService == null) {
            sender.sendMessage(plugin.messages.noGithubProfileService)
            return completedFuture(MissingServiceFailure(RPKGitHubProfileService::class.java))
        }
        val token = args[0]
        return supplyAsync {
            val ghUser = try {
                GitHub.connectUsingOAuth(token).myself
            } catch (exception: IOException) {
                sender.sendMessage(plugin.messages.profileLinkGithubInvalidToken)
                return@supplyAsync InvalidTokenFailure()
            }
            return@supplyAsync githubProfileService.createGitHubProfile(
                profile,
                RPKGitHubUsername(ghUser.login),
                token
            ).thenApply { githubProfile ->
                sender.sendMessage(plugin.messages.profileLinkGithubValid.withParameters(githubProfile = githubProfile))
                return@thenApply CommandSuccess
            }.join()
        }
    }

    class InvalidTokenFailure : CommandFailure()
}