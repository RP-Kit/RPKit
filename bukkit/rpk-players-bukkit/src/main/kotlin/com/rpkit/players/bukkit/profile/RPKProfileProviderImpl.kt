package com.rpkit.players.bukkit.profile

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.table.RPKProfileTable
import javax.servlet.http.HttpServletRequest


class RPKProfileProviderImpl(private val plugin: RPKPlayersBukkit): RPKProfileProvider {

    override fun getProfile(id: Int): RPKProfile? {
        return plugin.core.database.getTable(RPKProfileTable::class)[id]
    }

    override fun getProfile(name: String): RPKProfile? {
        return plugin.core.database.getTable(RPKProfileTable::class).get(name)
    }

    override fun addProfile(profile: RPKProfile) {
        plugin.core.database.getTable(RPKProfileTable::class).insert(profile)
    }

    override fun updateProfile(profile: RPKProfile) {
        plugin.core.database.getTable(RPKProfileTable::class).update(profile)
    }

    override fun removeProfile(profile: RPKProfile) {
        plugin.core.database.getTable(RPKProfileTable::class).delete(profile)
    }

    override fun getActiveProfile(req: HttpServletRequest): RPKProfile? {
        return req.session.getAttribute("profile") as? RPKProfile
    }

    override fun setActiveProfile(req: HttpServletRequest, profile: RPKProfile?) {
        req.session.setAttribute("profile", profile)
    }

}