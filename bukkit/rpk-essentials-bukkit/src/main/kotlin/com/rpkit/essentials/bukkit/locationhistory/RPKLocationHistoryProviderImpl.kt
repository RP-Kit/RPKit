package com.rpkit.essentials.bukkit.locationhistory

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.database.table.RPKPreviousLocationTable
import com.rpkit.locationhistory.bukkit.locationhistory.RPKLocationHistoryProvider
import com.rpkit.players.bukkit.player.RPKPlayer
import org.bukkit.Location


class RPKLocationHistoryProviderImpl(private val plugin: RPKEssentialsBukkit): RPKLocationHistoryProvider {

    override fun getPreviousLocation(player: RPKPlayer): Location? {
        return plugin.core.database.getTable(RPKPreviousLocationTable::class).get(player)?.location
    }

    override fun setPreviousLocation(player: RPKPlayer, location: Location) {
        val previousLocationTable = plugin.core.database.getTable(RPKPreviousLocationTable::class)
        var previousLocation = previousLocationTable.get(player)
        if (previousLocation != null) {
            previousLocation.location = location
            previousLocationTable.update(previousLocation)
        } else {
            previousLocation = RPKPreviousLocation(player = player, location = location)
            previousLocationTable.insert(previousLocation)
        }
    }

}