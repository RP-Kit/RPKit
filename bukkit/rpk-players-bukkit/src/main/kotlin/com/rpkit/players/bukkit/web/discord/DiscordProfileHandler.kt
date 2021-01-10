package com.rpkit.players.bukkit.web.discord

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.discord.DiscordUserId
import com.rpkit.players.bukkit.profile.discord.RPKDiscordProfile
import com.rpkit.players.bukkit.profile.discord.RPKDiscordProfileService
import com.rpkit.players.bukkit.web.ErrorResponse
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.lens.long

class DiscordProfileHandler {

    val idLens = Path.long().of("id")

    val profileIdLens = Query.int().required("profileId")

    fun get(request: Request): Response {
        val discordId = idLens(request)
        val discordProfileService = Services[RPKDiscordProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Discord profile service not found"))
        val discordProfile = discordProfileService.getDiscordProfile(DiscordUserId(discordId))
        return Response(OK)
            .with(DiscordProfileResponse.lens of discordProfile.toDiscordProfileResponse())
    }

    fun list(request: Request): Response {
        val profileId = profileIdLens(request)
        val profileService = Services[RPKProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Profile service not found"))
        val discordProfileService = Services[RPKDiscordProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Discord profile service not found"))
        val profile = profileService.getProfile(profileId)
            ?: return Response(Status.NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("Profile not found"))
        val discordProfiles = discordProfileService.getDiscordProfiles(profile)
        return Response(OK)
            .with(DiscordProfileResponse.listLens of discordProfiles.map(RPKDiscordProfile::toDiscordProfileResponse))
    }
}