package com.rpkit.locationhistory.bukkit.locationhistory

import com.rpkit.core.service.ServiceProvider
import com.rpkit.players.bukkit.player.RPKPlayer
import org.bukkit.Location


interface RPKLocationHistoryProvider: ServiceProvider {

    fun getPreviousLocation(player: RPKPlayer): Location?
    fun setPreviousLocation(player: RPKPlayer, location: Location)

}