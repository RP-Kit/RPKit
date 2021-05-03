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

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.github.RPKGitHubProfile
import com.rpkit.players.bukkit.profile.github.RPKGitHubProfileService
import com.rpkit.players.bukkit.profile.github.RPKGitHubUsername
import com.rpkit.players.bukkit.web.ErrorResponse
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.lens.string

class GitHubProfileHandler {

    val nameLens = Path.string().of("name")

    val profileIdLens = Query.int().required("profileId")

    fun get(request: Request): Response {
        val name = nameLens(request)
        val githubProfileService = Services[RPKGitHubProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("GitHub profile service not found"))
        val githubProfile = githubProfileService.getGitHubProfile(RPKGitHubUsername(name)).join()
            ?: return Response(NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("GitHub profile not found"))
        return Response(OK)
            .with(GitHubProfileResponse.lens of githubProfile.toGitHubProfileResponse())
    }

    fun list(request: Request): Response {
        val profileId = profileIdLens(request)
        val profileService = Services[RPKProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Profile service not found"))
        val githubProfileService = Services[RPKGitHubProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("GitHub profile service not found"))
        val profile = profileService.getProfile(RPKProfileId(profileId)).join()
            ?: return Response(NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("Profile not found"))
        val githubProfiles = githubProfileService.getGitHubProfiles(profile).join()
        return Response(OK)
            .with(GitHubProfileResponse.listLens of githubProfiles.map(RPKGitHubProfile::toGitHubProfileResponse))
    }

}