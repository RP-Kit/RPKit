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

package com.rpkit.players.bukkit.profile.irc

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.table.RPKIRCProfileTable
import com.rpkit.players.bukkit.event.ircprofile.RPKBukkitIRCProfileCreateEvent
import com.rpkit.players.bukkit.event.ircprofile.RPKBukkitIRCProfileDeleteEvent
import com.rpkit.players.bukkit.event.ircprofile.RPKBukkitIRCProfileUpdateEvent
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKThinProfile
import java.util.concurrent.CompletableFuture


class RPKIRCProfileServiceImpl(override val plugin: RPKPlayersBukkit) : RPKIRCProfileService {

    override fun getIRCProfile(id: RPKIRCProfileId): CompletableFuture<RPKIRCProfile?> {
        return plugin.database.getTable(RPKIRCProfileTable::class.java)[id]
    }

    override fun getIRCProfile(nick: RPKIRCNick): CompletableFuture<RPKIRCProfile?> {
        return plugin.database.getTable(RPKIRCProfileTable::class.java).get(nick)
    }

    override fun getIRCProfiles(profile: RPKProfile): CompletableFuture<List<RPKIRCProfile>> {
        return plugin.database.getTable(RPKIRCProfileTable::class.java).get(profile)
    }

    override fun addIRCProfile(profile: RPKIRCProfile): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitIRCProfileCreateEvent(profile, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            plugin.database.getTable(RPKIRCProfileTable::class.java).insert(event.ircProfile).join()
        }
    }

    override fun createIRCProfile(
        profile: RPKThinProfile,
        nick: RPKIRCNick
    ): CompletableFuture<RPKIRCProfile> {
        return CompletableFuture.supplyAsync {
            val ircProfile = RPKIRCProfileImpl(
                null,
                profile,
                nick
            )
            addIRCProfile(ircProfile).join()
            return@supplyAsync ircProfile
        }
    }

    override fun updateIRCProfile(profile: RPKIRCProfile): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitIRCProfileUpdateEvent(profile, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            plugin.database.getTable(RPKIRCProfileTable::class.java).update(event.ircProfile).join()
        }
    }

    override fun removeIRCProfile(profile: RPKIRCProfile): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitIRCProfileDeleteEvent(profile, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            plugin.database.getTable(RPKIRCProfileTable::class.java).delete(event.ircProfile).join()
        }
    }

}