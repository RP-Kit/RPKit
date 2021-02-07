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

package com.rpkit.players.bukkit.web.github

import com.rpkit.players.bukkit.profile.github.RPKGitHubProfile
import org.http4k.core.Body
import org.http4k.format.Gson.auto

data class GitHubProfileResponse(
    val id: Int,
    val profileId: Int,
    val name: String
) {
    companion object {
        val lens = Body.auto<GitHubProfileResponse>().toLens()
        val listLens = Body.auto<List<GitHubProfileResponse>>().toLens()
    }
}

fun RPKGitHubProfile.toGitHubProfileResponse() = GitHubProfileResponse(
    id?.value ?: 0,
    profile.id?.value ?: 0,
    name.value
)