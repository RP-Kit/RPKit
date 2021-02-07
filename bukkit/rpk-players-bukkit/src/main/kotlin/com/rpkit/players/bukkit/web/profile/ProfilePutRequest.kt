package com.rpkit.players.bukkit.web.profile

import org.http4k.core.Body
import org.http4k.format.Gson.auto

data class ProfilePutRequest(
    val name: String,
    val password: String
) {
    companion object {
        val lens = Body.auto<ProfilePutRequest>().toLens()
    }
}