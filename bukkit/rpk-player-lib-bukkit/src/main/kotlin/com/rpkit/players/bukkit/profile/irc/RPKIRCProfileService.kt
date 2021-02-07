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

package com.rpkit.players.bukkit.profile.irc

import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKThinProfile


interface RPKIRCProfileService : Service {

    fun getIRCProfile(id: RPKIRCProfileId): RPKIRCProfile?
    fun getIRCProfile(nick: RPKIRCNick): RPKIRCProfile?
    fun getIRCProfiles(profile: RPKProfile): List<RPKIRCProfile>
    fun addIRCProfile(profile: RPKIRCProfile)
    fun createIRCProfile(
        profile: RPKThinProfile,
        nick: RPKIRCNick
    ): RPKIRCProfile
    fun updateIRCProfile(profile: RPKIRCProfile)
    fun removeIRCProfile(profile: RPKIRCProfile)

}