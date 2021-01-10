package com.rpkit.players.bukkit.web.irc

import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.irc.RPKIRCProfile
import org.http4k.core.Body
import org.http4k.format.Gson.auto

data class IRCProfileResponse(
    val id: Int,
    val profileId: Int?,
    val nick: String
) {
    companion object {
        val lens = Body.auto<IRCProfileResponse>().toLens()
        val listLens = Body.auto<List<IRCProfileResponse>>().toLens()
    }
}

fun RPKIRCProfile.toIRCProfileResponse() = IRCProfileResponse(
    id ?: 0,
    (profile as? RPKProfile)?.id,
    nick.value
)