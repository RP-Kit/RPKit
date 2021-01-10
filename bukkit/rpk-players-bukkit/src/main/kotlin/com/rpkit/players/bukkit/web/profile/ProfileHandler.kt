package com.rpkit.players.bukkit.web.profile

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfileService
import com.rpkit.players.bukkit.web.ErrorResponse
import com.rpkit.players.bukkit.web.authenticatedProfile
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Path
import org.http4k.lens.int
import org.http4k.lens.string

class ProfileHandler {

    val nameLens = Path.string().of("name")
    val discriminatorLens = Path.int().of("discriminator")

    fun get(request: Request): Response {
        val name = nameLens(request)
        val discriminator = discriminatorLens(request)
        val profileService = Services[RPKProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Profile service not found"))
        val profile = profileService.getProfile(name, discriminator)
            ?: return Response(NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("Profile not found"))
        return Response(OK)
            .with(ProfileResponse.lens of profile.toProfileResponse())
    }

    fun put(request: Request): Response {
        val name = nameLens(request)
        val discriminator = discriminatorLens(request)
        val profilePutRequest = ProfilePutRequest.lens(request)
        val profileService = Services[RPKProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Profile service not found"))
        val profile = profileService.getProfile(name, discriminator)
            ?: return Response(NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("Profile not found"))
        if (profile != request.authenticatedProfile) {
            return Response(FORBIDDEN)
                .with(ErrorResponse.lens of ErrorResponse("You may not update other people's profiles."))
        }
        val newName = profilePutRequest.name
        profile.name = newName
        profile.discriminator = profileService.generateDiscriminatorFor(newName)
        profile.setPassword(profilePutRequest.password.toCharArray())
        profileService.updateProfile(profile)
        return Response(NO_CONTENT)
    }

    fun patch(request: Request): Response {
        val name = nameLens(request)
        val discriminator = discriminatorLens(request)
        val profilePatchRequest = ProfilePatchRequest.lens(request)
        val profileService = Services[RPKProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Profile service not found"))
        val profile = profileService.getProfile(name, discriminator)
            ?: return Response(NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("Profile not found"))
        if (profile != request.authenticatedProfile) {
            return Response(FORBIDDEN)
                .with(ErrorResponse.lens of ErrorResponse("You may not update other people's profiles."))
        }
        val newName = profilePatchRequest.name
        if (newName != null) {
            profile.name = newName
            profile.discriminator = profileService.generateDiscriminatorFor(newName)
        }
        if (profilePatchRequest.password != null) {
            profile.setPassword(profilePatchRequest.password.toCharArray())
        }
        profileService.updateProfile(profile)
        return Response(OK)
            .with(ProfileResponse.lens of profile.toProfileResponse())
    }

    fun post(request: Request): Response {
        val profileService = Services[RPKProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Profile service not found"))
        val profilePostRequest = ProfilePostRequest.lens(request)
        val profile = profileService.createProfile(profilePostRequest.name)
        if (profilePostRequest.password != null) {
            profile.setPassword(profilePostRequest.password.toCharArray())
            profileService.updateProfile(profile)
        }
        return Response(OK)
            .with(ProfileResponse.lens of profile.toProfileResponse())
    }

    fun delete(request: Request): Response {
        val name = nameLens(request)
        val discriminator = discriminatorLens(request)
        val profileService = Services[RPKProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Profile service not found"))
        val profile = profileService.getProfile(name, discriminator)
            ?: return Response(NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("Profile not found"))
        if (profile != request.authenticatedProfile) {
            return Response(FORBIDDEN)
                .with(ErrorResponse.lens of ErrorResponse("You may not delete other people's profiles."))
        }
        profileService.removeProfile(profile)
        return Response(NO_CONTENT)
    }

}