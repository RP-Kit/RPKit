package com.rpkit.players.bukkit.profile

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.database.table.RPKMinecraftProfileLinkRequestTable
import com.rpkit.players.bukkit.database.table.RPKMinecraftProfileTable
import com.rpkit.players.bukkit.database.table.RPKMinecraftProfileTokenTable
import com.rpkit.players.bukkit.event.minecraftprofile.RPKBukkitMinecraftProfileCreateEvent
import com.rpkit.players.bukkit.event.minecraftprofile.RPKBukkitMinecraftProfileDeleteEvent
import com.rpkit.players.bukkit.event.minecraftprofile.RPKBukkitMinecraftProfileUpdateEvent
import org.bukkit.OfflinePlayer


class RPKMinecraftProfileProviderImpl(private val plugin: RPKPlayersBukkit): RPKMinecraftProfileProvider {

    override fun getMinecraftProfile(id: Int): RPKMinecraftProfile? {
        return plugin.core.database.getTable(RPKMinecraftProfileTable::class)[id]
    }

    override fun getMinecraftProfile(player: OfflinePlayer): RPKMinecraftProfile? {
        return plugin.core.database.getTable(RPKMinecraftProfileTable::class).get(player)
    }

    override fun getMinecraftProfiles(profile: RPKProfile): List<RPKMinecraftProfile> {
        return plugin.core.database.getTable(RPKMinecraftProfileTable::class).get(profile)
    }

    override fun addMinecraftProfile(profile: RPKMinecraftProfile) {
        val event = RPKBukkitMinecraftProfileCreateEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKMinecraftProfileTable::class).insert(event.minecraftProfile)
    }

    override fun updateMinecraftProfile(profile: RPKMinecraftProfile) {
        val event = RPKBukkitMinecraftProfileUpdateEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKMinecraftProfileTable::class).update(event.minecraftProfile)
    }

    override fun removeMinecraftProfile(profile: RPKMinecraftProfile) {
        val event = RPKBukkitMinecraftProfileDeleteEvent(profile)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        plugin.core.database.getTable(RPKMinecraftProfileTable::class).delete(event.minecraftProfile)
    }

    override fun getMinecraftProfileToken(id: Int): RPKMinecraftProfileToken? {
        return plugin.core.database.getTable(RPKMinecraftProfileTokenTable::class)[id]
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

    override fun getMinecraftProfileLinkRequests(minecraftProfile: RPKMinecraftProfile): List<RPKMinecraftProfileLinkRequest> {
        return plugin.core.database.getTable(RPKMinecraftProfileLinkRequestTable::class).get(minecraftProfile)
    }

    override fun addMinecraftProfileLinkRequest(minecraftProfileLinkRequest: RPKMinecraftProfileLinkRequest) {
        plugin.core.database.getTable(RPKMinecraftProfileLinkRequestTable::class).insert(minecraftProfileLinkRequest)
    }

    override fun removeMinecraftProfileLinkRequest(minecraftProfileLinkRequest: RPKMinecraftProfileLinkRequest) {
        plugin.core.database.getTable(RPKMinecraftProfileLinkRequestTable::class).delete(minecraftProfileLinkRequest)
    }

    override fun updateMinecraftProfileLinkRequest(minecraftProfileLinkRequest: RPKMinecraftProfileLinkRequest) {
        plugin.core.database.getTable(RPKMinecraftProfileLinkRequestTable::class).update(minecraftProfileLinkRequest)
    }

}