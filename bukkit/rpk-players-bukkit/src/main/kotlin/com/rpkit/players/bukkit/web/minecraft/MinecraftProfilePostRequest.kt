package com.rpkit.players.bukkit.web.minecraft

import org.http4k.core.Body
import org.http4k.format.Gson.auto
import java.util.UUID

data class MinecraftProfilePostRequest(
    val minecraftUUID: UUID,
    val profileId: Int?
) {
    companion object {
        val lens = Body.auto<MinecraftProfilePostRequest>().toLens()
    }
}