package com.rpkit.players.bukkit.web.minecraft

import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import org.http4k.core.Body
import org.http4k.format.Gson.auto;
import java.util.UUID

data class MinecraftProfileResponse(
    val id: Int,
    val profileId: Int?,
    val minecraftUUID: UUID,
    val name: String,
    val isOnline: Boolean
) {
    companion object {
        val lens = Body.auto<MinecraftProfileResponse>().toLens()
        val listLens = Body.auto<List<MinecraftProfileResponse>>().toLens()
    }
}

fun RPKMinecraftProfile.toMinecraftProfileResponse() = MinecraftProfileResponse(
    id ?: 0,
    (profile as? RPKProfile)?.id,
    minecraftUUID,
    name,
    isOnline
)