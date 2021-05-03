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

package com.rpkit.players.bukkit.profile

import com.rpkit.core.service.Service
import java.util.concurrent.CompletableFuture

interface RPKProfileService : Service {

    fun getProfile(id: RPKProfileId): CompletableFuture<RPKProfile?>
    fun getProfile(name: RPKProfileName, discriminator: RPKProfileDiscriminator): CompletableFuture<RPKProfile?>
    fun addProfile(profile: RPKProfile): CompletableFuture<Void>
    fun updateProfile(profile: RPKProfile): CompletableFuture<Void>
    fun removeProfile(profile: RPKProfile): CompletableFuture<Void>
    fun createProfile(name: RPKProfileName, discriminator: RPKProfileDiscriminator? = null, password: String? = null): CompletableFuture<RPKProfile>
    fun createAndLoadProfile(name: RPKProfileName, discriminator: RPKProfileDiscriminator? = null, password: String? = null): CompletableFuture<RPKProfile>
    fun createThinProfile(name: RPKProfileName): RPKThinProfile
    fun generateDiscriminatorFor(name: RPKProfileName): CompletableFuture<RPKProfileDiscriminator>
    fun getPreloadedProfile(id: RPKProfileId): RPKProfile?
    fun getPreloadedProfile(name: RPKProfileName, discriminator: RPKProfileDiscriminator): RPKProfile?
    fun loadProfile(id: RPKProfileId): CompletableFuture<RPKProfile?>
    fun unloadProfile(profile: RPKProfile)

}