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

package com.rpkit.players.bukkit.profile

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.table.RPKProfileTable
import com.rpkit.players.bukkit.event.profile.RPKBukkitProfileCreateEvent
import com.rpkit.players.bukkit.event.profile.RPKBukkitProfileDeleteEvent
import com.rpkit.players.bukkit.event.profile.RPKBukkitProfileUpdateEvent


class RPKProfileServiceImpl(override val plugin: RPKPlayersBukkit) : RPKProfileService {

    override fun getProfile(id: RPKProfileId): RPKProfile? {
        return plugin.database.getTable(RPKProfileTable::class.java)[id]
    }

    override fun getProfile(name: RPKProfileName, discriminator: RPKProfileDiscriminator): RPKProfile? {
        return plugin.database.getTable(RPKProfileTable::class.java).get(name, discriminator)
    }

    override fun addProfile(profile: RPKProfile) {
        val event = RPKBukkitProfileCreateEvent(profile, !plugin.server.isPrimaryThread)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKProfileTable::class.java).insert(event.profile)
    }

    override fun updateProfile(profile: RPKProfile) {
        val event = RPKBukkitProfileUpdateEvent(profile, !plugin.server.isPrimaryThread)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKProfileTable::class.java).update(event.profile)
    }

    override fun removeProfile(profile: RPKProfile) {
        val event = RPKBukkitProfileDeleteEvent(profile, !plugin.server.isPrimaryThread)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKProfileTable::class.java).delete(event.profile)
    }

    override fun createProfile(name: RPKProfileName, discriminator: RPKProfileDiscriminator, password: String?): RPKProfile {
        val profile = RPKProfileImpl(name, discriminator, password)
        addProfile(profile)
        return profile
    }

    override fun createThinProfile(name: RPKProfileName): RPKThinProfile {
        return RPKThinProfileImpl(name)
    }

    override fun generateDiscriminatorFor(name: RPKProfileName): RPKProfileDiscriminator {
        return plugin.database.getTable(RPKProfileTable::class.java).generateDiscriminatorFor(name)
    }

}