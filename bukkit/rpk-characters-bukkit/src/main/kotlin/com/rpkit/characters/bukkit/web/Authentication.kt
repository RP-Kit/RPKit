package com.rpkit.characters.bukkit.web

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
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
            Services[RPKProfileService::class.java]?.getProfile(name, discriminator)
        }

fun authenticated() = ServerFilters.BasicAuth("rpkit", ::authenticate)

private fun authenticate(credentials: Credentials): Boolean {
    val profileService = Services[RPKProfileService::class.java] ?: return false
    val parts = credentials.user.split("#")
    if (parts.size < 2) return false
    val name = parts[0]
    val discriminator = parts[1].toIntOrNull() ?: return false
    val profile = profileService.getProfile(name, discriminator) ?: return false
    return profile.checkPassword(credentials.password.toCharArray())
}