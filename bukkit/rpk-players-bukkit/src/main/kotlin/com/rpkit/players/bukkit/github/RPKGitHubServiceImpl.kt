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

package com.rpkit.players.bukkit.github

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.github.RPKGitHubProfile
import org.kohsuke.github.GitHub
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.supplyAsync

class RPKGitHubServiceImpl(override val plugin: RPKPlayersBukkit) : RPKGitHubService {
    override fun submitIssue(profile: RPKGitHubProfile, title: String, body: String): CompletableFuture<out RPKGitHubIssue> = supplyAsync {
        val github = GitHub.connectUsingOAuth(profile.oauthToken)
        val ghIssue = github.getRepository("RP-Kit/RPKit").createIssue(title).body(body).create()
        return@supplyAsync RPKGitHubIssueImpl(
            ghIssue.title,
            ghIssue.body,
            ghIssue.htmlUrl.toString()
        )
    }
}