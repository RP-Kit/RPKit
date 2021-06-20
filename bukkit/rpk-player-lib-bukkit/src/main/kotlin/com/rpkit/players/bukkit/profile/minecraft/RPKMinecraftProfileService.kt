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

package com.rpkit.players.bukkit.profile.minecraft

import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKThinProfile
import org.bukkit.OfflinePlayer
import java.util.*
import java.util.concurrent.CompletableFuture


interface RPKMinecraftProfileService : Service {

    fun getMinecraftProfile(id: RPKMinecraftProfileId): CompletableFuture<RPKMinecraftProfile?>
    fun getMinecraftProfile(name: RPKMinecraftUsername): CompletableFuture<RPKMinecraftProfile?>
    fun getMinecraftProfile(player: OfflinePlayer): CompletableFuture<RPKMinecraftProfile?>
    fun getMinecraftProfile(minecraftUUID: UUID): CompletableFuture<RPKMinecraftProfile?>
    fun getMinecraftProfiles(profile: RPKProfile): CompletableFuture<List<RPKMinecraftProfile>>
    fun createMinecraftProfile(minecraftUsername: RPKMinecraftUsername, profile: RPKThinProfile? = null): CompletableFuture<RPKMinecraftProfile>
    fun createMinecraftProfile(minecraftUUID: UUID, profile: RPKThinProfile? = null): CompletableFuture<RPKMinecraftProfile>
    fun createAndLoadMinecraftProfile(minecraftUsername: RPKMinecraftUsername, profile: RPKThinProfile? = null): CompletableFuture<RPKMinecraftProfile>
    fun createAndLoadMinecraftProfile(minecraftUUID: UUID, profile: RPKThinProfile? = null): CompletableFuture<RPKMinecraftProfile>
    fun updateMinecraftProfile(profile: RPKMinecraftProfile): CompletableFuture<Void>
    fun removeMinecraftProfile(profile: RPKMinecraftProfile): CompletableFuture<Void>
    fun getMinecraftProfileLinkRequests(minecraftProfile: RPKMinecraftProfile): CompletableFuture<List<RPKMinecraftProfileLinkRequest>>
    fun removeMinecraftProfileLinkRequest(minecraftProfileLinkRequest: RPKMinecraftProfileLinkRequest): CompletableFuture<Void>
    fun createMinecraftProfileLinkRequest(profile: RPKProfile, minecraftProfile: RPKMinecraftProfile): CompletableFuture<RPKMinecraftProfileLinkRequest>
    fun getPreloadedMinecraftProfile(id: RPKMinecraftProfileId): RPKMinecraftProfile?
    fun getPreloadedMinecraftProfile(name: RPKMinecraftUsername): RPKMinecraftProfile?
    fun getPreloadedMinecraftProfile(minecraftUUID: UUID): RPKMinecraftProfile?
    fun getPreloadedMinecraftProfile(player: OfflinePlayer): RPKMinecraftProfile?
    fun loadMinecraftProfile(minecraftUUID: UUID): CompletableFuture<RPKMinecraftProfile?>
    fun unloadMinecraftProfile(minecraftProfile: RPKMinecraftProfile)

}