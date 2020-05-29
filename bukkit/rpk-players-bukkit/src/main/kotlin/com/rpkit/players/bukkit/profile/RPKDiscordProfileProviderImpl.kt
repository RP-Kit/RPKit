/*
 * Copyright 2020 Ren Binden
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

package com.rpkit.players.bukkit.profile

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.table.RPKDiscordProfileTable
import net.dv8tion.jda.api.entities.User

class RPKDiscordProfileProviderImpl(private val plugin: RPKPlayersBukkit): RPKDiscordProfileProvider {
    override fun getDiscordProfile(id: Int): RPKDiscordProfile? {
        return plugin.core.database.getTable(RPKDiscordProfileTable::class).get(id)
    }

    override fun getDiscordProfile(user: User): RPKDiscordProfile {
        val discordProfileTable = plugin.core.database.getTable(RPKDiscordProfileTable::class)
        var discordProfile = discordProfileTable.get(user)
        if (discordProfile == null) {
            discordProfile = RPKDiscordProfileImpl(discordId = user.idLong, profile = RPKThinProfileImpl(user.name))
            discordProfileTable.insert(discordProfile)
        }
        return discordProfile
    }

    override fun getDiscordProfiles(profile: RPKProfile): List<RPKDiscordProfile> {
        return plugin.core.database.getTable(RPKDiscordProfileTable::class).get(profile)
    }

    override fun addDiscordProfile(profile: RPKDiscordProfile) {
        plugin.core.database.getTable(RPKDiscordProfileTable::class).insert(profile)
    }

    override fun updateDiscordProfile(profile: RPKDiscordProfile) {
        plugin.core.database.getTable(RPKDiscordProfileTable::class).update(profile)
    }

    override fun removeDiscordProfile(profile: RPKDiscordProfile) {
        plugin.core.database.getTable(RPKDiscordProfileTable::class).delete(profile)
    }
}