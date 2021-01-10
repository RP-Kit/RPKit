package com.rpkit.players.bukkit.web.discord

import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.discord.RPKDiscordProfile
import org.http4k.core.Body
import org.http4k.format.Gson.auto

data class DiscordProfileResponse(
    val id: Int,
    val profileId: Int?,
    val discordId: Long
) {
    companion object {
        val lens = Body.auto<DiscordProfileResponse>().toLens()
        val listLens = Body.auto<List<DiscordProfileResponse>>().toLens()
    }
}

fun RPKDiscordProfile.toDiscordProfileResponse() = DiscordProfileResponse(
    id?.value ?: 0,
    (profile as? RPKProfile)?.id,
    discordId.value
)