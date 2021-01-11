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

package com.rpkit.players.bukkit.profile.github

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.table.RPKGitHubProfileTable
import com.rpkit.players.bukkit.event.githubprofile.RPKBukkitGitHubProfileCreateEvent
import com.rpkit.players.bukkit.event.githubprofile.RPKBukkitGitHubProfileDeleteEvent
import com.rpkit.players.bukkit.event.githubprofile.RPKBukkitGitHubProfileUpdateEvent
import com.rpkit.players.bukkit.profile.RPKProfile


class RPKGitHubProfileServiceImpl(override val plugin: RPKPlayersBukkit) : RPKGitHubProfileService {

    override fun getGitHubProfile(id: RPKGitHubProfileId): RPKGitHubProfile? {
        return plugin.database.getTable(RPKGitHubProfileTable::class.java)[id]
    }

    override fun getGitHubProfile(name: RPKGitHubUsername): RPKGitHubProfile? {
        return plugin.database.getTable(RPKGitHubProfileTable::class.java)[name]
    }

    override fun getGitHubProfiles(profile: RPKProfile): List<RPKGitHubProfile> {
        return plugin.database.getTable(RPKGitHubProfileTable::class.java).get(profile)
    }

    override fun addGitHubProfile(profile: RPKGitHubProfile) {
        val event = RPKBukkitGitHubProfileCreateEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKGitHubProfileTable::class.java).insert(event.githubProfile)
    }

    override fun updateGitHubProfile(profile: RPKGitHubProfile) {
        val event = RPKBukkitGitHubProfileUpdateEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKGitHubProfileTable::class.java).update(event.githubProfile)
    }

    override fun removeGitHubProfile(profile: RPKGitHubProfile) {
        val event = RPKBukkitGitHubProfileDeleteEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKGitHubProfileTable::class.java).delete(event.githubProfile)
    }

}