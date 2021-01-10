/*
 * Copyright 2020 Ren Binden
 *
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

package com.rpkit.players.bukkit.profile.minecraft

import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKThinProfile
import org.bukkit.OfflinePlayer
import java.util.UUID


interface RPKMinecraftProfileService : Service {

    fun getMinecraftProfile(id: Int): RPKMinecraftProfile?
    fun getMinecraftProfile(name: String): RPKMinecraftProfile?
    fun getMinecraftProfile(player: OfflinePlayer): RPKMinecraftProfile?
    fun getMinecraftProfile(minecraftUUID: UUID): RPKMinecraftProfile?
    fun getMinecraftProfiles(profile: RPKProfile): List<RPKMinecraftProfile>
    fun createMinecraftProfile(minecraftUsername: String, profile: RPKThinProfile? = null): RPKMinecraftProfile
    fun createMinecraftProfile(minecraftUUID: UUID, profile: RPKThinProfile? = null): RPKMinecraftProfile
    fun updateMinecraftProfile(profile: RPKMinecraftProfile)
    fun removeMinecraftProfile(profile: RPKMinecraftProfile)
    fun getMinecraftProfileLinkRequests(minecraftProfile: RPKMinecraftProfile): List<RPKMinecraftProfileLinkRequest>
    fun removeMinecraftProfileLinkRequest(minecraftProfileLinkRequest: RPKMinecraftProfileLinkRequest)
    fun createMinecraftProfileLinkRequest(profile: RPKProfile, minecraftProfile: RPKMinecraftProfile): RPKMinecraftProfileLinkRequest

}