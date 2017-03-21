package com.rpkit.players.bukkit.profile

import com.rpkit.core.service.ServiceProvider
import org.bukkit.OfflinePlayer


interface RPKMinecraftProfileProvider: ServiceProvider {

    fun getMinecraftProfile(id: Int): RPKMinecraftProfile?
    fun getMinecraftProfile(player: OfflinePlayer): RPKMinecraftProfile?
    fun getMinecraftProfiles(profile: RPKProfile): List<RPKMinecraftProfile>
    fun addMinecraftProfile(profile: RPKMinecraftProfile)
    fun updateMinecraftProfile(profile: RPKMinecraftProfile)
    fun removeMinecraftProfile(profile: RPKMinecraftProfile)
    fun getMinecraftProfileToken(id: Int): RPKMinecraftProfileToken?
    fun getMinecraftProfileToken(profile: RPKMinecraftProfile): RPKMinecraftProfileToken?
    fun addMinecraftProfileToken(token: RPKMinecraftProfileToken)
    fun updateMinecraftProfileToken(token: RPKMinecraftProfileToken)
    fun removeMinecraftProfileToken(token: RPKMinecraftProfileToken)

}