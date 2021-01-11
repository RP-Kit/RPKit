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

package com.rpkit.characters.bukkit.web

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileDiscriminator
import com.rpkit.players.bukkit.profile.RPKProfileName
import com.rpkit.players.bukkit.profile.RPKProfileService
import org.http4k.base64Decoded
import org.http4k.core.Credentials
import org.http4k.core.Request
import org.http4k.filter.ServerFilters

val Request.authenticatedProfile: RPKProfile?
    get() = header("Authorization")
        ?.trim()
        ?.takeIf { it.startsWith("Basic") }
        ?.substringAfter("Basic")
        ?.trim()
        ?.base64Decoded()
        ?.split(":")
        ?.getOrElse(0) { "" }
        ?.split("#")
        ?.let {
            if (it.size < 2) return@let null
            val name = it[0]
            val discriminator = it[1].toIntOrNull() ?: return@let null
            Services[RPKProfileService::class.java]?.getProfile(RPKProfileName(name), RPKProfileDiscriminator(discriminator))
        }

fun authenticated() = ServerFilters.BasicAuth("rpkit", ::authenticate)

private fun authenticate(credentials: Credentials): Boolean {
    val profileService = Services[RPKProfileService::class.java] ?: return false
    val parts = credentials.user.split("#")
    if (parts.size < 2) return false
    val name = parts[0]
    val discriminator = parts[1].toIntOrNull() ?: return false
    val profile = profileService.getProfile(RPKProfileName(name), RPKProfileDiscriminator(discriminator)) ?: return false
    return profile.checkPassword(credentials.password.toCharArray())
}