package com.rpkit.players.bukkit.web.irc

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.irc.IRCNick
import com.rpkit.players.bukkit.profile.irc.RPKIRCProfile
import com.rpkit.players.bukkit.profile.irc.RPKIRCProfileService
import com.rpkit.players.bukkit.web.ErrorResponse
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.lens.string

class IRCProfileHandler {

    val nickLens = Path.string().of("nick")

    val profileIdLens = Query.int().required("profileId")

    fun get(request: Request): Response {
        val nick = nickLens(request)
        val ircProfileService = Services[RPKIRCProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("IRC profile service not found"))
        val ircProfile = ircProfileService.getIRCProfile(IRCNick(nick))
            ?: return Response(NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("IRC profile not found"))
        return Response(OK)
            .with(IRCProfileResponse.lens of ircProfile.toIRCProfileResponse())
    }

    fun list(request: Request): Response {
        val profileId = profileIdLens(request)
        val profileService = Services[RPKProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Profile service not found"))
        val ircProfileService = Services[RPKIRCProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("IRC profile service not found"))
        val profile = profileService.getProfile(profileId)
            ?: return Response(NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("IRC profile not found"))
        val ircProfiles = ircProfileService.getIRCProfiles(profile)
        return Response(OK)
            .with(IRCProfileResponse.listLens of ircProfiles.map(RPKIRCProfile::toIRCProfileResponse))
    }

}