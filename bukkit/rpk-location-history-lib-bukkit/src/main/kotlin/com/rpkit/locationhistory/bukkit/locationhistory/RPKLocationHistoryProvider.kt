package com.rpkit.locationhistory.bukkit.locationhistory

import com.rpkit.core.service.ServiceProvider
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import org.bukkit.Location

/**
 * Provides history of locations of players, used with commands such as /back.
 */
interface RPKLocationHistoryProvider: ServiceProvider {

    /**
     * Gets the previous location of the player.
     * This is used with commands such as /back.
     * If the player does not have a previous location, null is returned.
     *
     * @param player The player
     * @return The player's previous location
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("getPreviousLocation(minecraftProfile)"))
    fun getPreviousLocation(player: RPKPlayer): Location?

    /**
     * Gets the previous location of the Minecraft profile.
     * This is used with commands such as /back.
     * If the player does not have a previous location, null is returned.
     *
     * @param minecraftProfile The Minecraft profile
     * @return The Minecraft profile's previous location
     */
    fun getPreviousLocation(minecraftProfile: RPKMinecraftProfile): Location?

    /**
     * Sets the previous location of the player.
     * This is used with commands such as /back.
     *
     * @param player The player
     * @param location The location to set the previous location to
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("setPreviousLocation(minecraftProfile, location)"))
    fun setPreviousLocation(player: RPKPlayer, location: Location)

    /**
     * Sets the previous location of the Minecraft profile.
     * This is used with commands such as /back.
     *
     * @param minecraftProfile The Minecraft profile
     * @param location The location to set the previous location to
     */
    fun setPreviousLocation(minecraftProfile: RPKMinecraftProfile, location: Location)

}