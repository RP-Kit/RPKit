package com.rpkit.players.bukkit.profile

import com.rpkit.core.service.ServiceProvider
import javax.servlet.http.HttpServletRequest

interface RPKProfileProvider: ServiceProvider {

    fun getProfile(id: Int): RPKProfile?
    fun getProfile(name: String): RPKProfile?
    fun addProfile(profile: RPKProfile)
    fun updateProfile(profile: RPKProfile)
    fun removeProfile(profile: RPKProfile)
    fun getActiveProfile(req: HttpServletRequest): RPKProfile?
    fun setActiveProfile(req: HttpServletRequest, profile: RPKProfile?)

}