package com.rpkit.players.bukkit.profile

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.table.RPKIRCProfileTable
import org.pircbotx.User


class RPKIRCProfileProviderImpl(private val plugin: RPKPlayersBukkit): RPKIRCProfileProvider {

    override fun getIRCProfile(id: Int): RPKIRCProfile? {
        return plugin.core.database.getTable(RPKIRCProfileTable::class).get(id)
    }

    override fun getIRCProfile(user: User): RPKIRCProfile? {
        return plugin.core.database.getTable(RPKIRCProfileTable::class).get(user)
    }

    override fun getIRCProfiles(profile: RPKProfile): List<RPKIRCProfile> {
        return plugin.core.database.getTable(RPKIRCProfileTable::class).get(profile)
    }

    override fun addIRCProfile(profile: RPKIRCProfile) {
        plugin.core.database.getTable(RPKIRCProfileTable::class).insert(profile)
    }

    override fun updateIRCProfile(profile: RPKIRCProfile) {
        plugin.core.database.getTable(RPKIRCProfileTable::class).update(profile)
    }

    override fun removeIRCProfile(profile: RPKIRCProfile) {
        plugin.core.database.getTable(RPKIRCProfileTable::class).delete(profile)
    }
    
}