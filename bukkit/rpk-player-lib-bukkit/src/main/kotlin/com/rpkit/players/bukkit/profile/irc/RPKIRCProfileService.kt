/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.players.bukkit.profile.irc

import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKThinProfile
import java.util.concurrent.CompletableFuture


interface RPKIRCProfileService : Service {

    fun getIRCProfile(id: RPKIRCProfileId): CompletableFuture<out RPKIRCProfile?>
    fun getIRCProfile(nick: RPKIRCNick): CompletableFuture<RPKIRCProfile?>
    fun getIRCProfiles(profile: RPKProfile): CompletableFuture<List<RPKIRCProfile>>
    fun addIRCProfile(profile: RPKIRCProfile): CompletableFuture<Void>
    fun createIRCProfile(
        profile: RPKThinProfile,
        nick: RPKIRCNick
    ): CompletableFuture<RPKIRCProfile>
    fun updateIRCProfile(profile: RPKIRCProfile): CompletableFuture<Void>
    fun removeIRCProfile(profile: RPKIRCProfile): CompletableFuture<Void>

}