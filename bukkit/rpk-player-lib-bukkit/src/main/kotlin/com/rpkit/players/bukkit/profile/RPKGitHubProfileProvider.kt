package com.rpkit.players.bukkit.profile

import com.rpkit.core.service.ServiceProvider
import org.kohsuke.github.GHUser


interface RPKGitHubProfileProvider: ServiceProvider {

    fun getGitHubProfile(id: Int): RPKGitHubProfile?
    fun getGitHubProfile(user: GHUser): RPKGitHubProfile?
    fun getGitHubProfiles(profile: RPKProfile): List<RPKGitHubProfile>
    fun addGitHubProfile(profile: RPKGitHubProfile)
    fun updateGitHubProfile(profile: RPKGitHubProfile)
    fun removeGitHubProfile(profile: RPKGitHubProfile)

}