package com.rpkit.players.bukkit.profile

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.table.RPKMinecraftProfileTable
import com.rpkit.players.bukkit.database.table.RPKMinecraftProfileTokenTable
import org.bukkit.OfflinePlayer


class RPKMinecraftProfileProviderImpl(private val plugin: RPKPlayersBukkit): RPKMinecraftProfileProvider {

    override fun getMinecraftProfile(id: Int): RPKMinecraftProfile? {
        return plugin.core.database.getTable(RPKMinecraftProfileTable::class).get(id)
    }

    override fun getMinecraftProfile(player: OfflinePlayer): RPKMinecraftProfile? {
        return plugin.core.database.getTable(RPKMinecraftProfileTable::class).get(player)
    }

    override fun getMinecraftProfiles(profile: RPKProfile): List<RPKMinecraftProfile> {
        return plugin.core.database.getTable(RPKMinecraftProfileTable::class).get(profile)
    }

    override fun addMinecraftProfile(profile: RPKMinecraftProfile) {
        plugin.core.database.getTable(RPKMinecraftProfileTable::class).insert(profile)
    }

    override fun updateMinecraftProfile(profile: RPKMinecraftProfile) {
        plugin.core.database.getTable(RPKMinecraftProfileTable::class).update(profile)
    }

    override fun removeMinecraftProfile(profile: RPKMinecraftProfile) {
        plugin.core.database.getTable(RPKMinecraftProfileTable::class).delete(profile)
    }

    override fun getMinecraftProfileToken(id: Int): RPKMinecraftProfileToken? {
        return plugin.core.database.getTable(RPKMinecraftProfileTokenTable::class).get(id)
    }

    override fun getMinecraftProfileToken(profile: RPKMinecraftProfile): RPKMinecraftProfileToken? {
        return plugin.core.database.getTable(RPKMinecraftProfileTokenTable::class).get(profile)
    }

    override fun addMinecraftProfileToken(token: RPKMinecraftProfileToken) {
        plugin.core.database.getTable(RPKMinecraftProfileTokenTable::class).insert(token)
    }

    override fun updateMinecraftProfileToken(token: RPKMinecraftProfileToken) {
        plugin.core.database.getTable(RPKMinecraftProfileTokenTable::class).update(token)
    }

    override fun removeMinecraftProfileToken(token: RPKMinecraftProfileToken) {
        plugin.core.database.getTable(RPKMinecraftProfileTokenTable::class).delete(token)
    }

}