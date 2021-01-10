package com.rpkit.players.bukkit.web.profile

import com.rpkit.players.bukkit.profile.RPKProfile
import org.http4k.core.Body
import org.http4k.format.Gson.auto

data class ProfileResponse(
    val id: Int,
    val name: String,
    val discriminator: Int
) {
    companion object {
        val lens = Body.auto<ProfileResponse>().toLens()
    }
}

fun RPKProfile.toProfileResponse() = ProfileResponse(
    id ?: 0,
    name,
    discriminator
)