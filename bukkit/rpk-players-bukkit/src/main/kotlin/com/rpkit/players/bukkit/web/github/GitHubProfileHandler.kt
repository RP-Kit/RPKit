package com.rpkit.players.bukkit.web.github

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.github.RPKGitHubProfile
import com.rpkit.players.bukkit.profile.github.RPKGitHubProfileService
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
        val githubProfile = githubProfileService.getGitHubProfile(name)
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
        val profile = profileService.getProfile(profileId)
            ?: return Response(NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("Profile not found"))
        val githubProfiles = githubProfileService.getGitHubProfiles(profile)
        return Response(OK)
            .with(GitHubProfileResponse.listLens of githubProfiles.map(RPKGitHubProfile::toGitHubProfileResponse))
    }

}