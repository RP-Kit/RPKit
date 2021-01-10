package com.rpkit.players.bukkit.web.minecraft

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.players.bukkit.web.ErrorResponse
import com.rpkit.players.bukkit.web.authenticatedProfile
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.core.Status.Companion.BAD_REQUEST
import org.http4k.core.Status.Companion.CONFLICT
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.int
import org.http4k.lens.string

class MinecraftProfileHandler {

    val nameLens = Path.string().of("name")
    val profileIdLens = Query.int().required("profileId")

    fun get(request: Request): Response {
        val name = nameLens(request)
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Minecraft profile service not found"))
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(name)
            ?: return Response(NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("Minecraft profile not found"))
        return Response(OK)
            .with(MinecraftProfileResponse.lens of minecraftProfile.toMinecraftProfileResponse())
    }

    fun post(request: Request): Response {
        val minecraftProfilePostRequest = MinecraftProfilePostRequest.lens(request)
        val profileService = Services[RPKProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Profile service not found"))
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Minecraft profile service not found"))
        val profile = if (minecraftProfilePostRequest.profileId != null) {
            val profile = profileService.getProfile(minecraftProfilePostRequest.profileId)
                ?: return Response(BAD_REQUEST)
                    .with(ErrorResponse.lens of ErrorResponse("Invalid profile ID"))
            if (profile != request.authenticatedProfile) {
                return Response(Status.FORBIDDEN)
                    .with(ErrorResponse.lens of ErrorResponse("You may not add Minecraft profiles to other players' profiles"))
            }
            profile
        } else null
        if (minecraftProfileService.getMinecraftProfile(minecraftProfilePostRequest.minecraftUUID) != null) {
            return Response(CONFLICT)
                .with(ErrorResponse.lens of ErrorResponse("A Minecraft profile with that UUID already exists"))
        }
        val minecraftProfile = minecraftProfileService.createMinecraftProfile(minecraftProfilePostRequest.minecraftUUID)
        if (profile != null) {
            minecraftProfileService.createMinecraftProfileLinkRequest(profile, minecraftProfile)
        }
        return Response(OK)
            .with(MinecraftProfileResponse.lens of minecraftProfile.toMinecraftProfileResponse())
    }

    fun list(request: Request): Response {
        val profileId = profileIdLens(request)
        val profileService = Services[RPKProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Profile service not found"))
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Minecraft profile service not found"))
        val profile = profileService.getProfile(profileId)
            ?: return Response(NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("Profile not found"))
        val minecraftProfiles = minecraftProfileService.getMinecraftProfiles(profile)
        return Response(OK)
            .with(MinecraftProfileResponse.listLens of minecraftProfiles.map(RPKMinecraftProfile::toMinecraftProfileResponse))
    }

}