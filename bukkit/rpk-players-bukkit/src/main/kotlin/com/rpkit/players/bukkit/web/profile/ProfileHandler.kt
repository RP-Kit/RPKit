/*
 * Copyright 2021 Ren Binden
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rpkit.players.bukkit.web.profile

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfileDiscriminator
import com.rpkit.players.bukkit.profile.RPKProfileName
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
        val profile = profileService.getProfile(RPKProfileName(name), RPKProfileDiscriminator(discriminator)).join()
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
        val profile = profileService.getProfile(RPKProfileName(name), RPKProfileDiscriminator(discriminator)).join()
            ?: return Response(NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("Profile not found"))
        if (profile != request.authenticatedProfile) {
            return Response(FORBIDDEN)
                .with(ErrorResponse.lens of ErrorResponse("You may not update other people's profiles."))
        }
        val newName = RPKProfileName(profilePutRequest.name)
        profile.name = newName
        profile.discriminator = profileService.generateDiscriminatorFor(newName).join()
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
        val profile = profileService.getProfile(RPKProfileName(name), RPKProfileDiscriminator(discriminator)).join()
            ?: return Response(NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("Profile not found"))
        if (profile != request.authenticatedProfile) {
            return Response(FORBIDDEN)
                .with(ErrorResponse.lens of ErrorResponse("You may not update other people's profiles."))
        }
        val newName = profilePatchRequest.name?.let(::RPKProfileName)
        if (newName != null) {
            profile.name = newName
            profile.discriminator = profileService.generateDiscriminatorFor(newName).join()
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
        val profile = profileService.createProfile(RPKProfileName(profilePostRequest.name)).join()
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
        val profile = profileService.getProfile(RPKProfileName(name), RPKProfileDiscriminator(discriminator)).join()
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