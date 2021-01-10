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
    id ?: 0,
    profile.id ?: 0,
    name
)