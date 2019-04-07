package com.rpkit.players.bukkit.profile

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.table.RPKGitHubProfileTable
import org.kohsuke.github.GHUser


class RPKGitHubProfileProviderImpl(private val plugin: RPKPlayersBukkit): RPKGitHubProfileProvider {

    override fun getGitHubProfile(id: Int): RPKGitHubProfile? {
        return plugin.core.database.getTable(RPKGitHubProfileTable::class)[id]
    }

    override fun getGitHubProfile(user: GHUser): RPKGitHubProfile? {
        return plugin.core.database.getTable(RPKGitHubProfileTable::class).get(user)
    }

    override fun getGitHubProfiles(profile: RPKProfile): List<RPKGitHubProfile> {
        return plugin.core.database.getTable(RPKGitHubProfileTable::class).get(profile)
    }

    override fun addGitHubProfile(profile: RPKGitHubProfile) {
        plugin.core.database.getTable(RPKGitHubProfileTable::class).insert(profile)
    }

    override fun updateGitHubProfile(profile: RPKGitHubProfile) {
        plugin.core.database.getTable(RPKGitHubProfileTable::class).update(profile)
    }

    override fun removeGitHubProfile(profile: RPKGitHubProfile) {
        plugin.core.database.getTable(RPKGitHubProfileTable::class).delete(profile)
    }

}