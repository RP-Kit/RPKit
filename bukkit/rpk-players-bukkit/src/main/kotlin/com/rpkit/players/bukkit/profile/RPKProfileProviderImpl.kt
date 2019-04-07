package com.rpkit.players.bukkit.profile

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.table.RPKProfileTable
import com.rpkit.players.bukkit.event.profile.RPKBukkitProfileCreateEvent
import com.rpkit.players.bukkit.event.profile.RPKBukkitProfileDeleteEvent
import com.rpkit.players.bukkit.event.profile.RPKBukkitProfileUpdateEvent
import javax.servlet.http.HttpServletRequest


class RPKProfileProviderImpl(private val plugin: RPKPlayersBukkit): RPKProfileProvider {

    override fun getProfile(id: Int): RPKProfile? {
        return plugin.core.database.getTable(RPKProfileTable::class)[id]
    }

    override fun getProfile(name: String): RPKProfile? {
        return plugin.core.database.getTable(RPKProfileTable::class).get(name)
    }

    override fun addProfile(profile: RPKProfile) {
        val event = RPKBukkitProfileCreateEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKProfileTable::class).insert(event.profile)
    }

    override fun updateProfile(profile: RPKProfile) {
        val event = RPKBukkitProfileUpdateEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKProfileTable::class).update(event.profile)
    }

    override fun removeProfile(profile: RPKProfile) {
        val event = RPKBukkitProfileDeleteEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKProfileTable::class).delete(event.profile)
    }

    override fun getActiveProfile(req: HttpServletRequest): RPKProfile? {
        return req.session.getAttribute("profile") as? RPKProfile
    }

    override fun setActiveProfile(req: HttpServletRequest, profile: RPKProfile?) {
        req.session.setAttribute("profile", profile)
    }

}