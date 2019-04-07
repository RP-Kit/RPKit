package com.rpkit.players.bukkit.profile

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.table.RPKIRCProfileTable
import com.rpkit.players.bukkit.event.ircprofile.RPKBukkitIRCProfileCreateEvent
import com.rpkit.players.bukkit.event.ircprofile.RPKBukkitIRCProfileDeleteEvent
import com.rpkit.players.bukkit.event.ircprofile.RPKBukkitIRCProfileUpdateEvent
import org.pircbotx.User


class RPKIRCProfileProviderImpl(private val plugin: RPKPlayersBukkit): RPKIRCProfileProvider {

    override fun getIRCProfile(id: Int): RPKIRCProfile? {
        return plugin.core.database.getTable(RPKIRCProfileTable::class)[id]
    }

    override fun getIRCProfile(user: User): RPKIRCProfile? {
        return plugin.core.database.getTable(RPKIRCProfileTable::class).get(user)
    }

    override fun getIRCProfiles(profile: RPKProfile): List<RPKIRCProfile> {
        return plugin.core.database.getTable(RPKIRCProfileTable::class).get(profile)
    }

    override fun addIRCProfile(profile: RPKIRCProfile) {
        val event = RPKBukkitIRCProfileCreateEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKIRCProfileTable::class).insert(event.ircProfile)
    }

    override fun updateIRCProfile(profile: RPKIRCProfile) {
        val event = RPKBukkitIRCProfileUpdateEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKIRCProfileTable::class).update(event.ircProfile)
    }

    override fun removeIRCProfile(profile: RPKIRCProfile) {
        val event = RPKBukkitIRCProfileDeleteEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKIRCProfileTable::class).delete(event.ircProfile)
    }
    
}