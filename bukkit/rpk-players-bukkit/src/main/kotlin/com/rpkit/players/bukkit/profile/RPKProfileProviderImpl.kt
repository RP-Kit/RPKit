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
import com.rpkit.players.bukkit.database.table.RPKProfileTable
import com.rpkit.players.bukkit.event.profile.RPKBukkitProfileCreateEvent
import com.rpkit.players.bukkit.event.profile.RPKBukkitProfileDeleteEvent
import com.rpkit.players.bukkit.event.profile.RPKBukkitProfileUpdateEvent
import javax.servlet.http.HttpServletRequest


class RPKProfileServiceImpl(override val plugin: RPKPlayersBukkit) : RPKProfileService {

    override fun getProfile(id: Int): RPKProfile? {
        return plugin.database.getTable(RPKProfileTable::class)[id]
    }

    override fun getProfile(name: String, discriminator: Int): RPKProfile? {
        return plugin.database.getTable(RPKProfileTable::class).get(name, discriminator)
    }

    override fun addProfile(profile: RPKProfile) {
        val event = RPKBukkitProfileCreateEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKProfileTable::class).insert(event.profile)
    }

    override fun updateProfile(profile: RPKProfile) {
        val event = RPKBukkitProfileUpdateEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKProfileTable::class).update(event.profile)
    }

    override fun removeProfile(profile: RPKProfile) {
        val event = RPKBukkitProfileDeleteEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.database.getTable(RPKProfileTable::class).delete(event.profile)
    }

    override fun generateDiscriminatorFor(name: String): Int {
        return plugin.database.getTable(RPKProfileTable::class).generateDiscriminatorFor(name)
    }

    override fun getActiveProfile(req: HttpServletRequest): RPKProfile? {
        return req.session.getAttribute("profile") as? RPKProfile
    }

    override fun setActiveProfile(req: HttpServletRequest, profile: RPKProfile?) {
        req.session.setAttribute("profile", profile)
    }

}