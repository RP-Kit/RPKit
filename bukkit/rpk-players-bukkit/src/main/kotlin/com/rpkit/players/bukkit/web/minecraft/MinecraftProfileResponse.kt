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
    id?.value ?: 0,
    (profile as? RPKProfile)?.id?.value,
    minecraftUUID,
    name,
    isOnline
)