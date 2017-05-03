package com.rpkit.players.bukkit.profile

import com.rpkit.core.service.ServiceProvider
import org.pircbotx.User


interface RPKIRCProfileProvider: ServiceProvider {

    fun getIRCProfile(id: Int): RPKIRCProfile?
    fun getIRCProfile(user: User): RPKIRCProfile?
    fun getIRCProfiles(profile: RPKProfile): List<RPKIRCProfile>
    fun addIRCProfile(profile: RPKIRCProfile)
    fun updateIRCProfile(profile: RPKIRCProfile)
    fun removeIRCProfile(profile: RPKIRCProfile)

}