package com.rpkit.essentials.bukkit.locationhistory

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.database.table.RPKPreviousLocationTable
import com.rpkit.locationhistory.bukkit.locationhistory.RPKLocationHistoryProvider
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.Location


class RPKLocationHistoryProviderImpl(private val plugin: RPKEssentialsBukkit): RPKLocationHistoryProvider {

    override fun getPreviousLocation(player: RPKPlayer): Location? {
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                return getPreviousLocation(minecraftProfile)
            }
        }
        return null
    }

    override fun getPreviousLocation(minecraftProfile: RPKMinecraftProfile): Location? {
        return plugin.core.database.getTable(RPKPreviousLocationTable::class).get(minecraftProfile)?.location
    }

    override fun setPreviousLocation(player: RPKPlayer, location: Location) {
        val bukkitPlayer = player.bukkitPlayer
        if (bukkitPlayer != null) {
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
            if (minecraftProfile != null) {
                setPreviousLocation(minecraftProfile, location)
            }
        }
    }

    override fun setPreviousLocation(minecraftProfile: RPKMinecraftProfile, location: Location) {
        val previousLocationTable = plugin.core.database.getTable(RPKPreviousLocationTable::class)
        var previousLocation = previousLocationTable.get(minecraftProfile)
        if (previousLocation != null) {
            previousLocation.location = location
            previousLocationTable.update(previousLocation)
        } else {
            previousLocation = RPKPreviousLocation(minecraftProfile = minecraftProfile, location = location)
            previousLocationTable.insert(previousLocation)
        }
    }

}