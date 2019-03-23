package com.rpkit.players.bukkit.profile

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.table.RPKGitHubProfileTable
import com.rpkit.players.bukkit.event.githubprofile.RPKBukkitGitHubProfileCreateEvent
import com.rpkit.players.bukkit.event.githubprofile.RPKBukkitGitHubProfileDeleteEvent
import com.rpkit.players.bukkit.event.githubprofile.RPKBukkitGitHubProfileUpdateEvent
import org.kohsuke.github.GHUser


class RPKGitHubProfileProviderImpl(private val plugin: RPKPlayersBukkit): RPKGitHubProfileProvider {

    override fun getGitHubProfile(id: Int): RPKGitHubProfile? {
        return plugin.core.database.getTable(RPKGitHubProfileTable::class).get(id)
    }

    override fun getGitHubProfile(user: GHUser): RPKGitHubProfile? {
        return plugin.core.database.getTable(RPKGitHubProfileTable::class).get(user)
    }

    override fun getGitHubProfiles(profile: RPKProfile): List<RPKGitHubProfile> {
        return plugin.core.database.getTable(RPKGitHubProfileTable::class).get(profile)
    }

    override fun addGitHubProfile(profile: RPKGitHubProfile) {
        val event = RPKBukkitGitHubProfileCreateEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKGitHubProfileTable::class).insert(event.githubProfile)
    }

    override fun updateGitHubProfile(profile: RPKGitHubProfile) {
        val event = RPKBukkitGitHubProfileUpdateEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKGitHubProfileTable::class).update(event.githubProfile)
    }

    override fun removeGitHubProfile(profile: RPKGitHubProfile) {
        val event = RPKBukkitGitHubProfileDeleteEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKGitHubProfileTable::class).delete(event.githubProfile)
    }

}